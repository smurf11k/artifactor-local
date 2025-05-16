package com.renata.infrastructure.persistence.contract;

import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.persistence.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Інтерфейс репозиторію для специфічних операцій з транзакціями. */
public interface TransactionRepository extends Repository<Transaction, UUID> {

    /**
     * Пошук транзакцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список транзакцій
     */
    List<Transaction> findByUserId(UUID userId);

    /**
     * Пошук транзакцій за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return список транзакцій
     */
    List<Transaction> findByItemId(UUID itemId);

    /**
     * Пошук транзакцій за типом.
     *
     * @param type тип транзакції
     * @return список транзакцій
     */
    List<Transaction> findByType(TransactionType type);

    /**
     * Пошук транзакцій за діапазоном дат.
     *
     * @param from початкова дата
     * @param to кінцева дата
     * @return список транзакцій
     */
    List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to);

    /**
     * Пошук транзакцій за користувачем та типом.
     *
     * @param userId ідентифікатор користувача
     * @param type тип транзакції
     * @return список транзакцій
     */
    List<Transaction> findByUserAndType(UUID userId, TransactionType type);
}
