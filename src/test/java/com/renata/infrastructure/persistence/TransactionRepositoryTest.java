package com.renata.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionRepositoryTest {

    @Mock private TransactionRepository transactionRepository;

    private Faker faker;
    private Transaction transaction;
    private UUID userId;
    private UUID itemId;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();
        timestamp = LocalDateTime.now().minusDays(1);

        transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setUserId(userId);
        transaction.setItemId(itemId);
        transaction.setType(TransactionType.PURCHASE);
        transaction.setTimestamp(timestamp);
    }

    @Test
    void findByUserId_ReturnsTransactionList() {
        when(transactionRepository.findByUserId(userId)).thenReturn(List.of(transaction));

        List<Transaction> result = transactionRepository.findByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(transactionRepository).findByUserId(userId);
    }

    @Test
    void findByItemId_ReturnsTransactionList() {
        when(transactionRepository.findByItemId(itemId)).thenReturn(List.of(transaction));

        List<Transaction> result = transactionRepository.findByItemId(itemId);

        assertEquals(1, result.size());
        assertEquals(itemId, result.get(0).getItemId());
        verify(transactionRepository).findByItemId(itemId);
    }

    @Test
    void findByType_ReturnsTransactionList() {
        TransactionType type = TransactionType.PURCHASE;
        when(transactionRepository.findByType(type)).thenReturn(List.of(transaction));

        List<Transaction> result = transactionRepository.findByType(type);

        assertEquals(1, result.size());
        assertEquals(type, result.get(0).getType());
        verify(transactionRepository).findByType(type);
    }

    @Test
    void findByDateRange_ReturnsTransactionList() {
        LocalDateTime from = LocalDateTime.now().minusDays(5);
        LocalDateTime to = LocalDateTime.now();

        when(transactionRepository.findByDateRange(from, to)).thenReturn(List.of(transaction));

        List<Transaction> result = transactionRepository.findByDateRange(from, to);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTimestamp().isAfter(from));
        assertTrue(result.get(0).getTimestamp().isBefore(to.plusSeconds(1)));
        verify(transactionRepository).findByDateRange(from, to);
    }
}
