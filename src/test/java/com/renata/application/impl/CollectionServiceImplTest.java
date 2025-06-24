package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.renata.application.contract.AuthService;
import com.renata.application.dto.CollectionStoreDto;
import com.renata.application.dto.CollectionUpdateDto;
import com.renata.application.exception.AuthException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.CollectionRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CollectionServiceImplTest {

    CollectionRepository collectionRepository;
    PersistenceContext persistenceContext;
    Validator validator;
    AuthService authService;
    CollectionServiceImpl collectionService;

    @BeforeEach
    void setUp() {
        collectionRepository = mock(CollectionRepository.class);
        persistenceContext = mock(PersistenceContext.class);
        validator = mock(Validator.class);
        authService = mock(AuthService.class);
        collectionService =
                new CollectionServiceImpl(
                        collectionRepository, persistenceContext, validator, authService);
    }

    @Test
    void create_validDto_registersAndCommits() {
        UUID userId = UUID.randomUUID();
        CollectionStoreDto dto = mock(CollectionStoreDto.class);
        when(dto.userId()).thenReturn(userId);
        when(dto.name()).thenReturn("MyCollection");
        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        Collection created = collectionService.create(dto);

        assertNotNull(created.getId());
        assertEquals(userId, created.getUserId());
        assertEquals("MyCollection", created.getName());
        assertNotNull(created.getCreatedAt());

        verify(persistenceContext).registerNew(created);
        verify(persistenceContext).commit();
    }

    @Test
    void create_invalidDto_throwsValidationException() {
        CollectionStoreDto dto = mock(CollectionStoreDto.class);
        Set<ConstraintViolation<CollectionStoreDto>> violations =
                Collections.singleton(mock(ConstraintViolation.class));
        when(validator.validate(dto)).thenReturn(violations);

        assertThrows(ValidationException.class, () -> collectionService.create(dto));

        verifyNoInteractions(persistenceContext);
    }

    @Test
    void update_validDto_authorized_updatesAndCommits() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CollectionUpdateDto dto = mock(CollectionUpdateDto.class);
        when(dto.id()).thenReturn(id);
        when(dto.name()).thenReturn("UpdatedName");
        when(dto.userId()).thenReturn(userId);
        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        Collection existing = new Collection();
        existing.setId(id);
        existing.setUserId(userId);

        when(collectionRepository.findById(id)).thenReturn(Optional.of(existing));

        // Create a mock User and return correct userId
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(authService.getCurrentUser()).thenReturn(user);

        Collection updated = collectionService.update(dto);

        assertEquals("UpdatedName", updated.getName());
        assertEquals(userId, updated.getUserId());

        verify(persistenceContext).registerUpdated(id, existing);
        verify(persistenceContext).commit();
    }

    @Test
    void update_unauthorized_throwsAuthException() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();

        CollectionUpdateDto dto = mock(CollectionUpdateDto.class);
        when(dto.id()).thenReturn(id);
        when(dto.userId()).thenReturn(userId);
        when(dto.name()).thenReturn("Name");
        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        Collection existing = new Collection();
        existing.setId(id);
        existing.setUserId(differentUserId);

        when(collectionRepository.findById(id)).thenReturn(Optional.of(existing));

        // Mock User with userId different from existing collection's userId
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(authService.getCurrentUser()).thenReturn(user);

        assertThrows(AuthException.class, () -> collectionService.update(dto));

        verify(persistenceContext, never()).registerUpdated(any(), any());
        verify(persistenceContext, never()).commit();
    }

    @Test
    void update_nonexistentCollection_throwsDatabaseAccessException() {
        UUID id = UUID.randomUUID();

        CollectionUpdateDto dto = mock(CollectionUpdateDto.class);
        when(dto.id()).thenReturn(id);
        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(authService.getCurrentUser()).thenReturn(user);

        when(collectionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () ->
                        collectionService.update(
                                dto)); // DatabaseAccessException extends RuntimeException

        verify(persistenceContext, never()).registerUpdated(any(), any());
        verify(persistenceContext, never()).commit();
    }

    @Test
    void delete_existingCollection_registersDeletedAndCommits() throws AuthException {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Collection collection = new Collection();
        collection.setId(id);
        collection.setUserId(userId);

        when(collectionRepository.findById(id)).thenReturn(Optional.of(collection));

        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(authService.getCurrentUser()).thenReturn(user);
        when(authService.hasPermission(Role.EntityName.COLLECTION, "delete")).thenReturn(true);

        collectionService.delete(id);

        verify(collectionRepository).clearCollection(id);
        verify(persistenceContext).registerDeleted(collection);
        verify(persistenceContext).commit();
    }

    @Test
    void delete_nonexistentCollection_doesNothing() {
        UUID id = UUID.randomUUID();

        when(collectionRepository.findById(id)).thenReturn(Optional.empty());

        collectionService.delete(id);

        verify(collectionRepository, never()).clearCollection(any());
        verify(persistenceContext, never()).registerDeleted(any());
        verify(persistenceContext, never()).commit();
    }
}
