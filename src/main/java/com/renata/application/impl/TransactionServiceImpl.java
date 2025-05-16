package com.renata.application.impl;

import com.renata.application.contract.TransactionService;
import com.renata.application.dto.TransactionStoreDto;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Реалізація сервісу для управління транзакціями. */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final PersistenceContext persistenceContext;

    /**
     * Конструктор для ін'єкції залежностей.
     *
     * @param transactionRepository репозиторій транзакцій
     * @param persistenceContext контекст для управління транзакціями
     */
    public TransactionServiceImpl(
            TransactionRepository transactionRepository, PersistenceContext persistenceContext) {
        this.transactionRepository = transactionRepository;
        this.persistenceContext = persistenceContext;
    }

    /**
     * Creates a new transaction from DTO.
     *
     * @param transactionStoreDto DTO containing transaction data
     * @return created transaction
     * @throws DatabaseAccessException if database error occurs
     */
    @Override
    public Transaction create(TransactionStoreDto transactionStoreDto) {
        // Convert DTO to Entity using builder pattern (recommended)
        Transaction transaction =
                Transaction.builder()
                        .id(UUID.randomUUID())
                        .userId(transactionStoreDto.userId())
                        .itemId(transactionStoreDto.itemId())
                        .type(transactionStoreDto.type())
                        .timestamp(
                                transactionStoreDto.timestamp() != null
                                        ? transactionStoreDto.timestamp()
                                        : LocalDateTime.now())
                        .build();

        persistenceContext.registerNew(transaction);
        persistenceContext.commit();
        return transaction;
    }

    /**
     * Оновлює існуючу транзакцію.
     *
     * @param id ідентифікатор транзакції для оновлення
     * @param transaction оновлені дані транзакції
     * @return оновлена транзакція
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    @Override
    public Transaction update(UUID id, Transaction transaction) {
        transaction.setId(id);

        persistenceContext.registerUpdated(id, transaction);
        persistenceContext.commit();
        return transaction;
    }

    /**
     * Видаляє транзакцію.
     *
     * @param id ідентифікатор транзакції для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     */
    @Override
    public void delete(UUID id) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            persistenceContext.registerDeleted(transaction);
            persistenceContext.commit();
        }
    }

    /**
     * Знаходить транзакцію за ідентифікатором.
     *
     * @param id ідентифікатор транзакції
     * @return Optional з транзакцією, якщо знайдено
     */
    @Override
    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    /**
     * Знаходить всі транзакції з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit кількість записів для отримання
     * @return список транзакцій
     */
    @Override
    public List<Transaction> findAll(int offset, int limit) {
        return transactionRepository.findAll(offset, limit);
    }

    /**
     * Знаходить транзакції за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список транзакцій
     */
    @Override
    public List<Transaction> findByUserId(UUID userId) {
        return transactionRepository.findByUserId(userId);
    }

    /**
     * Знаходить транзакції за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return список транзакцій
     */
    @Override
    public List<Transaction> findByItemId(UUID itemId) {
        return transactionRepository.findByItemId(itemId);
    }

    /**
     * Знаходить транзакції за типом.
     *
     * @param type тип транзакції
     * @return список транзакцій
     */
    @Override
    public List<Transaction> findByType(TransactionType type) {
        return transactionRepository.findByType(type);
    }

    /**
     * Знаходить транзакції за діапазоном дат.
     *
     * @param from початкова дата
     * @param to кінцева дата
     * @return список транзакцій
     */
    @Override
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return transactionRepository.findByDateRange(from, to);
    }

    /**
     * Знаходить транзакції за користувачем та типом.
     *
     * @param userId ідентифікатор користувача
     * @param type тип транзакції
     * @return список транзакцій
     */
    @Override
    public List<Transaction> findByUserAndType(UUID userId, TransactionType type) {
        return transactionRepository.findByUserAndType(userId, type);
    }
}
