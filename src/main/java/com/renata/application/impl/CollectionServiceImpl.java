package com.renata.application.impl;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.CollectionService;
import com.renata.application.dto.CollectionStoreDto;
import com.renata.application.dto.CollectionUpdateDto;
import com.renata.application.exception.AuthException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
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

/** Реалізація сервісу для управління колекціями антикваріату. */
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

        Collection collection = new Collection();
        collection.setId(UUID.randomUUID());
        collection.setUserId(collectionStoreDto.userId());
        collection.setName(collectionStoreDto.name());
        collection.setCreatedAt(LocalDateTime.now());

        persistenceContext.registerNew(collection);
        persistenceContext.commit();
        System.out.println(
                "Збережено колекцію: " + collection + ", власник: " + collection.getUserId());
        return collection;
    }

    @Override
    public Collection update(CollectionUpdateDto collectionUpdateDto) {
        Set<jakarta.validation.ConstraintViolation<CollectionUpdateDto>> violations =
                validator.validate(collectionUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("collection update", violations);
        }

        UUID dtoId = collectionUpdateDto.id();
        User user = authService.getCurrentUser();

        Optional<Collection> collectionOpt = collectionRepository.findById(dtoId);
        if (collectionOpt.isEmpty()) {
            throw new DatabaseAccessException("Колекцію не знайдено з таким id: " + dtoId);
        }
        Collection collection = collectionOpt.get();

        boolean isOwner = collection.getUserId().equals(user.getId());
        boolean isAdmin =
                authService.hasPermission(Role.EntityName.COLLECTION, "update")
                        && user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AuthException("У вас немає права на редагування цієї колекції.");
        }

        collection.setName(collectionUpdateDto.name());

        persistenceContext.registerUpdated(dtoId, collection);
        persistenceContext.commit();
        return collection;
    }

    @Override
    public void delete(UUID id) {
        Optional<Collection> collectionOpt = collectionRepository.findById(id);
        if (collectionOpt.isEmpty()) {
            return;
        }

        Collection collection = collectionOpt.get();
        User user = authService.getCurrentUser();

        boolean isOwner = collection.getUserId().equals(user.getId());
        boolean isAdmin =
                authService.hasPermission(Role.EntityName.COLLECTION, "delete")
                        && user.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AuthException("У вас немає права на видалення цієї колекції.");
        }

        collectionRepository.clearCollection(id);

        persistenceContext.registerDeleted(collection);
        persistenceContext.commit();
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

    @Override
    public List<Collection> findByName(String name) {
        return collectionRepository.findByName(name);
    }
}
