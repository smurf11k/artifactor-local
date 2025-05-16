package com.renata.application.impl;

import com.renata.application.contract.CollectionService;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.CollectionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final PersistenceContext persistenceContext;

    public CollectionServiceImpl(
            CollectionRepository collectionRepository, PersistenceContext persistenceContext) {
        this.collectionRepository = collectionRepository;
        this.persistenceContext = persistenceContext;
    }

    @Override
    public Collection create(Collection collection) {
        if (collection.getId() == null) {
            collection.setId(UUID.randomUUID());
        }

        collection.setCreatedAt(LocalDateTime.now());

        persistenceContext.registerNew(collection);
        persistenceContext.commit();
        return collection;
    }

    @Override
    public Collection update(UUID id, Collection collection) {
        collection.setId(id);

        persistenceContext.registerUpdated(id, collection);
        persistenceContext.commit();
        return collection;
    }

    @Override
    public void delete(UUID id) {
        Optional<Collection> collectionOpt = collectionRepository.findById(id);
        if (collectionOpt.isPresent()) {
            Collection collection = collectionOpt.get();

            collectionRepository.clearCollection(id);

            persistenceContext.registerDeleted(collection);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<Collection> findById(UUID id) {
        return collectionRepository.findById(id);
    }

    @Override
    public List<Collection> findAll(int offset, int limit) {
        return collectionRepository.findAll(offset, limit);
    }

    @Override
    public List<Collection> findByUserId(UUID userId) {
        return collectionRepository.findByUserId(userId);
    }

    @Override
    public List<Item> findItemsByCollectionId(UUID collectionId) {
        return collectionRepository.findItemsByCollectionId(collectionId);
    }

    @Override
    public void attachItemToCollection(UUID collectionId, UUID itemId) {
        collectionRepository.attachItemToCollection(collectionId, itemId);
        persistenceContext.commit();
    }

    @Override
    public void detachItemFromCollection(UUID collectionId, UUID itemId) {
        collectionRepository.detachItemFromCollection(collectionId, itemId);
        persistenceContext.commit();
    }

    @Override
    public void clearCollection(UUID collectionId) {
        collectionRepository.clearCollection(collectionId);
        persistenceContext.commit();
    }
}
