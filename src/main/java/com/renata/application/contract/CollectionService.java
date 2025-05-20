package com.renata.application.contract;

import com.renata.application.dto.CollectionStoreDto;
import com.renata.application.dto.CollectionUpdateDto;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Інтерфейс для управління колекціями антикваріату. */
public interface CollectionService {
    /**
     * Створює нову колекцію.
     *
     * @param collectionStoreDto DTO з даними для створення колекції
     * @return створена колекція
     */
    Collection create(CollectionStoreDto collectionStoreDto);

    /**
     * Оновлює існуючу колекцію.
     *
     * @param id ідентифікатор колекції для оновлення
     * @param collectionUpdateDto DTO з даними для оновлення колекції
     * @return оновлена колекція
     */
    Collection update(UUID id, CollectionUpdateDto collectionUpdateDto);

    /**
     * Видаляє колекцію за ідентифікатором.
     *
     * @param id ідентифікатор колекції для видалення
     */
    void delete(UUID id);

    /**
     * Знаходить колекцію за ідентифікатором.
     *
     * @param id ідентифікатор колекції
     * @return Optional з колекцією, якщо знайдено
     */
    Optional<Collection> findById(UUID id);

    /**
     * Отримує список колекцій з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit кількість записів для отримання
     * @return список колекцій
     */
    List<Collection> findAll(int offset, int limit);

    /**
     * Знаходить колекції, пов'язані з конкретним користувачем.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій користувача
     */
    List<Collection> findByUserId(UUID userId);

    /**
     * Отримує всі елементи конкретної колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return список елементів у колекції
     */
    List<Item> findItemsByCollectionId(UUID collectionId);

    /**
     * Додає елемент до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param itemId ідентифікатор елемента для додавання
     */
    void attachItemToCollection(UUID collectionId, UUID itemId);

    /**
     * Видаляє елемент з колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param itemId ідентифікатор елемента для видалення
     */
    void detachItemFromCollection(UUID collectionId, UUID itemId);

    /**
     * Очищає колекцію, видаляючи всі її елементи.
     *
     * @param collectionId ідентифікатор колекції для очищення
     */
    void clearCollection(UUID collectionId);
}
