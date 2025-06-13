package com.renata.application.impl;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.CollectionService;
import com.renata.application.dto.CollectionStoreDto;
import com.renata.application.dto.CollectionUpdateDto;
import com.renata.application.exception.AuthException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.CollectionRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
final class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final PersistenceContext persistenceContext;
    private final Validator validator;
    private final AuthService authService;

    public CollectionServiceImpl(
            CollectionRepository collectionRepository,
            PersistenceContext persistenceContext,
            Validator validator,
            AuthService authService) {
        this.collectionRepository = collectionRepository;
        this.persistenceContext = persistenceContext;
        this.validator = validator;
        this.authService = authService;
    }

    @Override
    public Collection create(CollectionStoreDto collectionStoreDto) {
        Set<ConstraintViolation<CollectionStoreDto>> violations =
                validator.validate(collectionStoreDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("collection creation", violations);
        }

        UUID userId;
        try {
            userId = authService.getCurrentUser().getId();
            System.out.println("Authenticated userId: " + userId);
        } catch (AuthException e) {
            System.out.println("Failed to get current user: " + e.getMessage());
            throw e;
        }

        if (userId == null) {
            System.out.println("Error: userId is null after authentication");
            throw new AuthException("Authenticated user has no ID");
        }

        Collection collection = new Collection();
        collection.setId(UUID.randomUUID());
        collection.setUserId(userId);
        collection.setName(collectionStoreDto.name());
        collection.setCreatedAt(LocalDateTime.now());

        System.out.println(
                "Saving collection: id="
                        + collection.getId()
                        + ", userId="
                        + collection.getUserId()
                        + ", name="
                        + collection.getName());
        persistenceContext.registerNew(collection);
        persistenceContext.commit();
        System.out.println("Saved entity: " + collection + ", userId: " + collection.getUserId());
        return collection;
    }

    @Override
    public Collection update(UUID id, CollectionUpdateDto collectionUpdateDto) {
        Set<jakarta.validation.ConstraintViolation<CollectionUpdateDto>> violations =
                validator.validate(collectionUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("collection update", violations);
        }

        UUID userId = authService.getCurrentUser().getId();

        Optional<Collection> collectionOpt = collectionRepository.findById(id);
        if (collectionOpt.isEmpty()) {
            throw new DatabaseAccessException("Collection not found with id: " + id);
        }
        Collection collection = collectionOpt.get();

        if (!collection.getUserId().equals(userId)) {
            throw new AuthException("You are not authorized to update this collection");
        }

        collection.setName(collectionUpdateDto.name());
        collection.setUserId(collectionUpdateDto.userId());

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
