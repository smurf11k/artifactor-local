package com.renata.application.contract;

import com.renata.application.dto.TransactionStoreDto;
import com.renata.application.dto.TransactionUpdateDto;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Інтерфейс для роботи з транзакціями антикваріату. */
public interface TransactionService {
    /**
     * Створює нову транзакцію.
     *
     * @param transactionStoreDto DTO з даними для створення транзакції
     * @return створена транзакція
     */
    Transaction create(TransactionStoreDto transactionStoreDto);

    /**
     * Оновлює існуючу транзакцію.
     *
     * @param id ідентифікатор транзакції
     * @param transactionUpdateDto оновлені дані транзакції
     * @return оновлена транзакція
     */
    Transaction update(UUID id, TransactionUpdateDto transactionUpdateDto);

    /**
     * Видаляє транзакцію.
     *
     * @param id ідентифікатор транзакції для видалення
     */
    void delete(UUID id);

    /**
     * Знаходить транзакцію за ідентифікатором.
     *
     * @param id ідентифікатор транзакції
     * @return Optional з транзакцією, якщо знайдено
     */
    Optional<Transaction> findById(UUID id);

    /**
     * Отримує список транзакцій з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit кількість записів для отримання
     * @return список транзакцій
     */
    List<Transaction> findAll(int offset, int limit);

    /**
     * Знаходить транзакції за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return транзакцій користувача
     */
    List<Transaction> findByUserId(UUID userId);

    /**
     * Знаходить транзакції за ідентифікатором предмета.
     *
     * @param itemId ідентифікатор предмета антикваріату
     * @return список список транзакцій предмета
     */
    List<Transaction> findByItemId(UUID itemId);

    /**
     * Фільтрує транзакції за типом (купівля/продаж).
     *
     * @param type тип транзакції
     * @return список транзакцій вказаного типу
     */
    List<Transaction> findByType(TransactionType type);

    /**
     * Знаходить транзакції за часовим діапазоном.
     *
     * @param from початкова дата діапазону
     * @param to кінцева дата діапазону
     * @return список транзакцій у вказаному діапазоні
     */
    List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to);

    /**
     * Фільтрує транзакції за користувачем та типом.
     *
     * @param userId ідентифікатор користувача
     * @param type тип транзакції
     * @return список транзакцій, що відповідають критеріям
     */
    List<Transaction> findByUserAndType(UUID userId, TransactionType type);
}
