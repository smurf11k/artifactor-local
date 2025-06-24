package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.renata.application.contract.PasswordService;
import com.renata.application.dto.UserStoreDto;
import com.renata.application.exception.SignUpException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @Mock private Validator validator;

    @Mock private PasswordService passwordService;

    @InjectMocks private UserServiceImpl userService;

    private UUID userId;
    private User user;
    private UserStoreDto userStoreDto;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = new User(userId, "testuser", "hashedpass", "test@example.com", Role.GENERAL);
        userStoreDto = new UserStoreDto("newuser", "password123", "new@example.com", Role.GENERAL);
    }

    @Test
    void findById_existingUser_returnsUser() {
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        User found = userService.findById(userId);
        assertEquals(user, found);
    }

    @Test
    void findById_userNotFound_throwsException() {
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    void findByUsername_existingUser_returnsUser() {
        when(userRepository.findByUsername("testuser")).thenReturn(List.of(user));
        User found = userService.findByUsername("testuser");
        assertEquals(user, found);
    }

    @Test
    void findByUsername_noUser_throwsException() {
        when(userRepository.findByUsername("missing")).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class, () -> userService.findByUsername("missing"));
    }

    @Test
    void findByEmail_existingUser_returnsUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(List.of(user));
        User found = userService.findByEmail("test@example.com");
        assertEquals(user, found);
    }

    @Test
    void findByEmail_noUser_throwsException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(List.of());
        assertThrows(
                EntityNotFoundException.class,
                () -> userService.findByEmail("missing@example.com"));
    }

    @Test
    void findAll_returnsUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> users = userService.findAll();
        assertEquals(1, users.size());
        assertEquals(user, users.get(0));
    }

    @Test
    void findByPartialUsername_returnsUserList() {
        when(userRepository.findByPartialUsername("test")).thenReturn(List.of(user));
        List<User> users = userService.findByPartialUsername("test");
        assertEquals(1, users.size());
    }

    @Test
    void existsByUsername_returnsTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.existsByUsername("testuser"));
    }

    @Test
    void existsByEmail_returnsTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(userService.existsByEmail("test@example.com"));
    }

    @Test
    void create_validUser_returnsSavedUser() {
        when(validator.validate(userStoreDto)).thenReturn(Set.of());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordService.hash("password123")).thenReturn("hashedpass123");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenReturn(user);

        User created = userService.create(userStoreDto);

        assertEquals(user, created);
        User savedUser = captor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("hashedpass123", savedUser.getPasswordHash());
        assertEquals("new@example.com", savedUser.getEmail());
    }

    @Test
    void create_validationFails_throwsValidationException() {
        ConstraintViolation<UserStoreDto> violation = mock(ConstraintViolation.class);
        when(validator.validate(userStoreDto)).thenReturn(Set.of(violation));
        assertThrows(ValidationException.class, () -> userService.create(userStoreDto));
    }

    @Test
    void create_usernameExists_throwsSignUpException() {
        when(validator.validate(userStoreDto)).thenReturn(Set.of());
        when(userRepository.existsByUsername("newuser")).thenReturn(true);
        assertThrows(SignUpException.class, () -> userService.create(userStoreDto));
    }

    @Test
    void create_emailExists_throwsSignUpException() {
        when(validator.validate(userStoreDto)).thenReturn(Set.of());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        assertThrows(SignUpException.class, () -> userService.create(userStoreDto));
    }

    @Test
    void countCollectionsByUserId_returnsCount() {
        when(userRepository.countCollectionsByUserId(userId)).thenReturn(5L);
        long count = userService.countCollectionsByUserId(userId);
        assertEquals(5L, count);
    }

    @Test
    void findCollectionsByUserId_returnsCollections() {
        Collection collection = new Collection();
        when(userRepository.findCollectionsByUserId(userId)).thenReturn(List.of(collection));
        List<Collection> collections = userService.findCollectionsByUserId(userId);
        assertEquals(1, collections.size());
        assertEquals(collection, collections.get(0));
    }
}
