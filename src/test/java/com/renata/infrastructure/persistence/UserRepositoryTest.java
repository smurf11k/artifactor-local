package com.renata.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.infrastructure.persistence.contract.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock private UserRepository userRepository;

    private Faker faker;
    private User user;
    private Collection collection;
    private UUID userId;
    private String username;
    private String email;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        userId = UUID.randomUUID();
        username = faker.name().username();
        email = faker.internet().emailAddress();

        user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEmail(email);

        collection = new Collection();
        collection.setId(UUID.randomUUID());
        collection.setUserId(userId);
        collection.setName(faker.lorem().word());
        collection.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByUsername_ReturnsUserList() {
        when(userRepository.findByUsername(username)).thenReturn(List.of(user));

        List<User> result = userRepository.findByUsername(username);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void findByEmail_ReturnsUserList() {
        when(userRepository.findByEmail(email)).thenReturn(List.of(user));

        List<User> result = userRepository.findByEmail(email);

        assertEquals(1, result.size());
        assertEquals(email, result.get(0).getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findCollectionsByUserId_ReturnsCollectionList() {
        when(userRepository.findCollectionsByUserId(userId)).thenReturn(List.of(collection));

        List<Collection> result = userRepository.findCollectionsByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(collection.getId(), result.get(0).getId());
        verify(userRepository).findCollectionsByUserId(userId);
    }

    @Test
    void findByPartialUsername_ReturnsUserList() {
        String partialUsername = username.substring(0, 3);
        when(userRepository.findByPartialUsername(partialUsername)).thenReturn(List.of(user));

        List<User> result = userRepository.findByPartialUsername(partialUsername);

        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
        verify(userRepository).findByPartialUsername(partialUsername);
    }

    @Test
    void countCollectionsByUserId_ReturnsCount() {
        long expectedCount = 5;
        when(userRepository.countCollectionsByUserId(userId)).thenReturn(expectedCount);

        long result = userRepository.countCollectionsByUserId(userId);

        assertEquals(expectedCount, result);
        verify(userRepository).countCollectionsByUserId(userId);
    }

    @Test
    void existsByUsername_ReturnsTrue() {
        when(userRepository.existsByUsername(username)).thenReturn(true);

        boolean result = userRepository.existsByUsername(username);

        assertTrue(result);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        when(userRepository.existsByEmail(email)).thenReturn(true);

        boolean result = userRepository.existsByEmail(email);

        assertTrue(result);
        verify(userRepository).existsByEmail(email);
    }
}
