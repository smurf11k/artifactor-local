package com.renata.infrastructure.persistence.contract;

import com.renata.domain.entities.Collection;
import com.renata.infrastructure.persistence.Repository;
import java.util.List;
import java.util.UUID;

/** Інтерфейс репозиторію для специфічних операцій з колекціями. */
public interface CollectionRepository extends Repository<Collection, UUID> {

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    List<Collection> findByUserId(UUID userId);

    /**
     * Пошук колекцій за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return список колекцій
     */
    List<Collection> findByItemId(UUID itemId);

    /**
     * Прикріплення антикваріату до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param itemId ідентифікатор антикваріату
     */
    void attachItemToCollection(UUID collectionId, UUID itemId);

    /**
     * Від'єднання антикваріату від колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param itemId ідентифікатор антикваріату
     */
    void detachItemFromCollection(UUID collectionId, UUID itemId);

    /**
     * Підрахунок антикваріату у колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return кількість антикваріату
     */
    long countItemsByCollectionId(UUID collectionId);

    /**
     * Пошук колекцій за назвою.
     *
     * @param name назва колекції
     * @return список колекцій
     */
    List<Collection> findByName(String name);

    /**
     * Видалення всіх антикваріату із колекції.
     *
     * @param collectionId ідентифікатор колекції
     */
    void clearCollection(UUID collectionId);
}
