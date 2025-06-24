package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.renata.application.dto.MarketInfoStoreDto;
import com.renata.application.dto.MarketInfoUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.MarketInfoRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarketInfoServiceImplTest {

    MarketInfoRepository repository;
    PersistenceContext persistenceContext;
    Validator validator;
    MarketInfoServiceImpl service;

    @BeforeEach
    void setup() {
        repository = mock(MarketInfoRepository.class);
        persistenceContext = mock(PersistenceContext.class);
        validator = mock(Validator.class);

        service = new MarketInfoServiceImpl(repository, persistenceContext, validator);
    }

    @Test
    void create_registersNewMarketInfo_andCommits() {
        MarketInfoStoreDto dto =
                new MarketInfoStoreDto(
                        123.45, UUID.randomUUID(), LocalDateTime.now(), MarketEventType.PURCHASED);

        MarketInfo created = service.create(dto);

        assertEquals(dto.price(), created.getPrice());
        assertEquals(dto.itemId(), created.getItemId());
        assertEquals(dto.type(), created.getType());
        assertNotNull(created.getTimestamp());

        verify(persistenceContext).registerNew(created);
        verify(persistenceContext).commit();
    }

    @Test
    void update_validDto_updatesMarketInfo() {
        UUID id = UUID.randomUUID();
        MarketInfoUpdateDto dto = mock(MarketInfoUpdateDto.class);

        when(dto.id()).thenReturn(id);
        when(dto.price()).thenReturn(99.9);
        when(dto.itemId()).thenReturn(UUID.randomUUID());
        when(dto.type()).thenReturn(MarketEventType.RELISTED);
        LocalDateTime ts = LocalDateTime.now();
        when(dto.timestamp()).thenReturn(ts);

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        MarketInfo existing =
                MarketInfo.builder()
                        .id(id)
                        .price(50)
                        .itemId(UUID.randomUUID())
                        .type(MarketEventType.LISTED)
                        .timestamp(LocalDateTime.now().minusDays(1))
                        .build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        MarketInfo updated = service.update(dto);

        assertEquals(dto.price(), updated.getPrice());
        assertEquals(dto.itemId(), updated.getItemId());
        assertEquals(dto.type(), updated.getType());
        assertEquals(dto.timestamp(), updated.getTimestamp());

        verify(persistenceContext).registerUpdated(id, updated);
        verify(persistenceContext).commit();
    }

    @Test
    void update_invalidDto_throwsValidationException() {
        MarketInfoUpdateDto dto = mock(MarketInfoUpdateDto.class);

        Set<ConstraintViolation<MarketInfoUpdateDto>> violations =
                Set.of(mock(ConstraintViolation.class));
        when(validator.validate(dto)).thenReturn(violations);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.update(dto));
        assertTrue(ex.getMessage().contains("market info update"));

        verifyNoInteractions(repository);
        verifyNoInteractions(persistenceContext);
    }

    @Test
    void update_nonExistingId_throwsDatabaseAccessException() {
        UUID id = UUID.randomUUID();
        MarketInfoUpdateDto dto = mock(MarketInfoUpdateDto.class);

        when(dto.id()).thenReturn(id);
        when(validator.validate(dto)).thenReturn(Collections.emptySet());
        when(repository.findById(id)).thenReturn(Optional.empty());

        DatabaseAccessException ex =
                assertThrows(DatabaseAccessException.class, () -> service.update(dto));
        assertTrue(ex.getMessage().contains("Не знайдено ринкової інформації з таким id"));

        verify(persistenceContext, never()).registerUpdated(any(), any());
        verify(persistenceContext, never()).commit();
    }

    @Test
    void delete_existingMarketInfo_registersDeletedAndCommits() {
        UUID id = UUID.randomUUID();
        MarketInfo existing = MarketInfo.builder().id(id).build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(persistenceContext).registerDeleted(existing);
        verify(persistenceContext).commit();
    }

    @Test
    void delete_nonExistingMarketInfo_doesNothing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        service.delete(id);

        verifyNoInteractions(persistenceContext);
    }

    @Test
    void findById_delegatesToRepository() {
        UUID id = UUID.randomUUID();
        MarketInfo info = MarketInfo.builder().id(id).build();
        when(repository.findById(id)).thenReturn(Optional.of(info));

        Optional<MarketInfo> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(info, result.get());
    }

    @Test
    void findAll_delegatesToRepository() {
        List<MarketInfo> list = List.of(MarketInfo.builder().build());
        when(repository.findAll(0, 10)).thenReturn(list);

        List<MarketInfo> result = service.findAll(0, 10);

        assertEquals(list, result);
    }

    @Test
    void findByItemId_delegatesToRepository() {
        UUID itemId = UUID.randomUUID();
        List<MarketInfo> list = List.of(MarketInfo.builder().itemId(itemId).build());
        when(repository.findByItemId(itemId)).thenReturn(list);

        List<MarketInfo> result = service.findByItemId(itemId);

        assertEquals(list, result);
    }

    @Test
    void findByEventType_delegatesToRepository() {
        List<MarketInfo> list =
                List.of(MarketInfo.builder().type(MarketEventType.PURCHASED).build());
        when(repository.findByEventType(MarketEventType.PURCHASED)).thenReturn(list);

        List<MarketInfo> result = service.findByEventType(MarketEventType.PURCHASED);

        assertEquals(list, result);
    }

    @Test
    void findByDateRange_delegatesToRepository() {
        LocalDateTime from = LocalDateTime.now().minusDays(5);
        LocalDateTime to = LocalDateTime.now();
        List<MarketInfo> list = List.of(MarketInfo.builder().timestamp(to.minusDays(1)).build());

        when(repository.findByDateRange(from, to)).thenReturn(list);

        List<MarketInfo> result = service.findByDateRange(from, to);

        assertEquals(list, result);
    }

    @Test
    void findLatestMarketInfo_returnsLatest() {
        UUID itemId = UUID.randomUUID();
        MarketInfo older = MarketInfo.builder().timestamp(LocalDateTime.now().minusDays(2)).build();
        MarketInfo latest = MarketInfo.builder().timestamp(LocalDateTime.now()).build();

        List<MarketInfo> infos = List.of(older, latest);
        when(repository.findByItemId(itemId)).thenReturn(infos);

        Optional<MarketInfo> result = service.findLatestMarketInfo(itemId);

        assertTrue(result.isPresent());
        assertEquals(latest.getTimestamp(), result.get().getTimestamp());
    }

    @Test
    void deleteOlderThan_callsRepositoryAndCommits() {
        LocalDateTime olderThan = LocalDateTime.now().minusDays(10);

        service.deleteOlderThan(olderThan);

        verify(repository).deleteOlderThan(olderThan);
        verify(persistenceContext).commit();
    }
}
