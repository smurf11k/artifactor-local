package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.contract.UserService;
import com.renata.application.dto.TransactionStoreDto;
import com.renata.application.dto.TransactionUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.entities.Transaction;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.domain.enums.MarketEventType;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class TransactionServiceImplTest {

    @Mock TransactionRepository transactionRepository;
    @Mock MarketInfoService marketInfoService;
    @Mock UserService userService;
    @Mock AuthService authService;
    @Mock ItemService itemService;
    @Mock PersistenceContext persistenceContext;
    @Mock Validator validator;
    @Mock InfrastructureConfig infrastructureConfig;

    @InjectMocks TransactionServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_success_registersTransactionAndMarketInfo() {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        TransactionType type = TransactionType.PURCHASE;
        LocalDateTime timestamp = LocalDateTime.now();

        TransactionStoreDto dto = new TransactionStoreDto(userId, itemId, type, timestamp);

        MarketInfo latestMarketInfo =
                MarketInfo.builder()
                        .id(UUID.randomUUID())
                        .itemId(itemId)
                        .price(100.0)
                        .timestamp(LocalDateTime.now().minusDays(1))
                        .type(MarketEventType.RELISTED)
                        .build();

        when(marketInfoService.findLatestMarketInfo(itemId))
                .thenReturn(Optional.of(latestMarketInfo));

        Transaction created = service.create(dto);

        assertNotNull(created.getId());
        assertEquals(userId, created.getUserId());
        assertEquals(itemId, created.getItemId());
        assertEquals(type, created.getType());
        assertEquals(timestamp, created.getTimestamp());

        verify(persistenceContext).registerNew(any(Transaction.class));
        verify(persistenceContext).registerNew(any(MarketInfo.class));
        verify(persistenceContext).commit();
    }

    @Test
    void create_throwsIfNoMarketInfo() {
        UUID itemId = UUID.randomUUID();
        TransactionStoreDto dto =
                new TransactionStoreDto(UUID.randomUUID(), itemId, TransactionType.PURCHASE, null);

        when(marketInfoService.findLatestMarketInfo(itemId)).thenReturn(Optional.empty());

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> service.create(dto));
        assertTrue(ex.getMessage().contains("Не знайдено ринкової інформації"));
    }

    @Test
    void update_validInput_updatesTransaction() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        TransactionType type = TransactionType.SALE;
        LocalDateTime timestamp = LocalDateTime.now();

        TransactionUpdateDto dto = new TransactionUpdateDto(id, userId, itemId, type, timestamp);

        Transaction existing =
                Transaction.builder()
                        .id(id)
                        .userId(userId)
                        .itemId(UUID.randomUUID())
                        .type(TransactionType.PURCHASE)
                        .timestamp(LocalDateTime.now().minusDays(1))
                        .build();

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));

        User adminUser = new User(UUID.randomUUID(), "admin", "hash", "email", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(authService.hasPermission(Role.EntityName.TRANSACTION, "update")).thenReturn(true);

        Transaction updated = service.update(dto);

        assertEquals(type, updated.getType());
        assertEquals(itemId, updated.getItemId());
        assertEquals(timestamp, updated.getTimestamp());

        verify(persistenceContext).registerUpdated(id, updated);
        verify(persistenceContext).commit();
    }

    @Test
    void update_invalidInput_throwsValidationException() {
        TransactionUpdateDto dto =
                new TransactionUpdateDto(
                        UUID.randomUUID(),
                        null, // invalid: null userId
                        UUID.randomUUID(),
                        TransactionType.PURCHASE,
                        LocalDateTime.now());

        Set<ConstraintViolation<TransactionUpdateDto>> violations =
                Set.of(mock(ConstraintViolation.class));

        when(validator.validate(dto)).thenReturn(violations);

        assertThrows(ValidationException.class, () -> service.update(dto));
    }

    @Test
    void update_transactionNotFound_throwsDatabaseAccessException() {
        UUID id = UUID.randomUUID();
        TransactionUpdateDto dto =
                new TransactionUpdateDto(
                        id,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        TransactionType.PURCHASE,
                        LocalDateTime.now());

        when(validator.validate(dto)).thenReturn(Collections.emptySet());
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.update(dto));
    }

    @Test
    void delete_existingTransaction_registersDeleted() {
        UUID id = UUID.randomUUID();
        Transaction transaction =
                Transaction.builder()
                        .id(id)
                        .userId(UUID.randomUUID())
                        .itemId(UUID.randomUUID())
                        .type(TransactionType.PURCHASE)
                        .timestamp(LocalDateTime.now())
                        .build();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

        User adminUser = new User(UUID.randomUUID(), "admin", "hash", "email", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(authService.hasPermission(Role.EntityName.TRANSACTION, "delete")).thenReturn(true);

        service.delete(id);

        verify(persistenceContext).registerDeleted(transaction);
        verify(persistenceContext).commit();
    }

    @Test
    void delete_nonExistingTransaction_noAction() {
        UUID id = UUID.randomUUID();

        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        User adminUser = new User(UUID.randomUUID(), "admin", "hash", "email", Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(adminUser);
        when(authService.hasPermission(Role.EntityName.TRANSACTION, "delete")).thenReturn(true);

        service.delete(id);

        verify(persistenceContext, never()).registerDeleted(any());
        verify(persistenceContext, never()).commit();
    }

    @Test
    void findById_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        TransactionType type = TransactionType.PURCHASE;
        LocalDateTime timestamp = LocalDateTime.now();

        Transaction tx =
                Transaction.builder()
                        .id(id)
                        .userId(userId)
                        .itemId(itemId)
                        .type(type)
                        .timestamp(timestamp)
                        .build();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(tx));

        Optional<Transaction> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(tx, result.get());
    }

    @Test
    void findAll_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        TransactionType type = TransactionType.PURCHASE;
        LocalDateTime timestamp = LocalDateTime.now();

        Transaction tx =
                Transaction.builder()
                        .id(id)
                        .userId(userId)
                        .itemId(itemId)
                        .type(type)
                        .timestamp(timestamp)
                        .build();

        List<Transaction> list = List.of(tx);
        when(transactionRepository.findAll(0, 10)).thenReturn(list);

        List<Transaction> result = service.findAll(0, 10);

        assertEquals(list, result);
    }

    @Test
    void generateReport_createsFileSuccessfully() throws Exception {
        Transaction tx =
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .userId(UUID.randomUUID())
                        .itemId(UUID.randomUUID())
                        .type(TransactionType.PURCHASE)
                        .timestamp(LocalDateTime.now())
                        .build();

        when(transactionRepository.findAll(0, Integer.MAX_VALUE)).thenReturn(List.of(tx));
        when(authService.getCurrentUser())
                .thenReturn(
                        new com.renata.domain.entities.User(
                                tx.getUserId(), "testuser", "", "", null));
        when(userService.findById(tx.getUserId()))
                .thenReturn(
                        new com.renata.domain.entities.User(
                                tx.getUserId(), "testuser", "", "", null));

        com.renata.domain.entities.Item item = new com.renata.domain.entities.Item();
        item.setId(tx.getItemId());
        item.setName("itemName");
        when(itemService.findById(tx.getItemId())).thenReturn(Optional.of(item));

        when(marketInfoService.findLatestMarketInfo(tx.getItemId()))
                .thenReturn(Optional.of(MarketInfo.builder().price(123.45).build()));
        when(infrastructureConfig.getReportsDirectory()).thenReturn("target/reports");

        File dir = new File("target/reports");
        if (!dir.exists()) dir.mkdirs();

        assertDoesNotThrow(() -> service.generateReport(t -> true));
    }

    @Test
    void generateReport_throwsRuntimeIfCannotCreateDir() {
        when(infrastructureConfig.getReportsDirectory())
                .thenReturn("Z:\\this\\path\\cannot\\be\\created");
        when(authService.getCurrentUser())
                .thenReturn(
                        new com.renata.domain.entities.User(
                                UUID.randomUUID(), "testuser", "", "", null));

        RuntimeException ex =
                assertThrows(RuntimeException.class, () -> service.generateReport(t -> true));
        System.out.println("Exception message: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("Не вдалося створити директорію"));
    }
}
