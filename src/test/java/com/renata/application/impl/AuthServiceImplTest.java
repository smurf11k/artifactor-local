package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.renata.application.contract.PasswordService;
import com.renata.application.exception.AuthException;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.persistence.contract.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceImplTest {

    UserRepository userRepository;
    PasswordService passwordService;
    AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);
        authService = new AuthServiceImpl(userRepository, passwordService);
    }

    @Test
    void login_success_setsCurrentUser() throws AuthException {
        String username = "user1";
        String rawPassword = "pass";
        String hashedPassword = "hashed";

        UUID userId = UUID.randomUUID();
        User user = new User(userId, username, hashedPassword, "email@example.com", Role.GENERAL);

        when(userRepository.findByUsername(username)).thenReturn(List.of(user));
        when(passwordService.verify(rawPassword, hashedPassword)).thenReturn(true);

        boolean result = authService.login(username, rawPassword);

        assertTrue(result);
        assertTrue(authService.isAuthenticated());
        assertEquals(user, authService.getCurrentUser());
    }

    @Test
    void login_wrongPassword_returnsFalse() throws AuthException {
        String username = "user1";
        String rawPassword = "wrong";
        String hashedPassword = "hashed";

        User user =
                new User(
                        UUID.randomUUID(),
                        username,
                        hashedPassword,
                        "email@example.com",
                        Role.GENERAL);

        when(userRepository.findByUsername(username)).thenReturn(List.of(user));
        when(passwordService.verify(rawPassword, hashedPassword)).thenReturn(false);

        boolean result = authService.login(username, rawPassword);

        assertFalse(result);
        assertFalse(authService.isAuthenticated());
    }

    @Test
    void login_alreadyLoggedIn_throwsAuthException() throws AuthException {
        User user = new User(UUID.randomUUID(), "user1", "hash", "email", Role.GENERAL);
        when(userRepository.findByUsername("user1")).thenReturn(List.of(user));
        when(passwordService.verify(anyString(), anyString())).thenReturn(true);
        authService.login("user1", "pass");

        AuthException ex =
                assertThrows(AuthException.class, () -> authService.login("user2", "pass2"));
        assertTrue(ex.getMessage().contains("Ви вже авторизовані"));
    }

    @Test
    void logout_clearsCurrentUser() throws AuthException {
        User user = new User(UUID.randomUUID(), "user", "hash", "email@example.com", Role.GENERAL);
        when(userRepository.findByUsername("user")).thenReturn(List.of(user));
        when(passwordService.verify("pass", "hash")).thenReturn(true);

        boolean loginResult = authService.login("user", "pass");
        assertTrue(loginResult, "Login should succeed");
        assertTrue(authService.isAuthenticated(), "User should be authenticated after login");

        authService.logout();
        assertFalse(authService.isAuthenticated(), "User should not be authenticated after logout");

        AuthException ex = assertThrows(AuthException.class, () -> authService.getCurrentUser());
        assertTrue(ex.getMessage().contains("Ви не авторизовані"));
    }

    @Test
    void logout_whenNotAuthenticated_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class, () -> authService.logout());
        assertTrue(ex.getMessage().contains("No active session"));
    }

    @Test
    void getCurrentUser_whenNotAuthenticated_throwsAuthException() {
        AuthException ex = assertThrows(AuthException.class, () -> authService.getCurrentUser());
        assertTrue(ex.getMessage().contains("Ви не авторизовані"));
    }

    @Test
    void hasPermission_checksCorrectly() throws AuthException {
        User user = new User(UUID.randomUUID(), "user", "hash", "email", Role.GENERAL);
        when(userRepository.findByUsername("user")).thenReturn(List.of(user));
        when(passwordService.verify(anyString(), anyString())).thenReturn(true);

        authService.login("user", "pass");

        assertFalse(authService.hasPermission(Role.EntityName.ITEM, "create"));
        assertTrue(authService.hasPermission(Role.EntityName.ITEM, "read"));
        assertFalse(authService.hasPermission(Role.EntityName.ITEM, "delete"));
        assertFalse(authService.hasPermission(Role.EntityName.MARKET, "create"));
        assertTrue(authService.hasPermission(Role.EntityName.COLLECTION, "UPDATE"));
    }

    @Test
    void hasPermission_unknownAction_returnsFalse() throws AuthException {
        User user = new User(UUID.randomUUID(), "user", "hash", "email", Role.GENERAL);
        when(userRepository.findByUsername("user")).thenReturn(List.of(user));
        when(passwordService.verify(anyString(), anyString())).thenReturn(true);

        authService.login("user", "pass");

        assertFalse(authService.hasPermission(Role.EntityName.ITEM, "nonexistentAction"));
    }

    @Test
    void validatePermission_grantsPermission() throws AuthException {
        User user = new User(UUID.randomUUID(), "user", "hash", "email", Role.ADMIN);
        when(userRepository.findByUsername("user")).thenReturn(List.of(user));
        when(passwordService.verify(anyString(), anyString())).thenReturn(true);

        authService.login("user", "pass");

        assertDoesNotThrow(() -> authService.validatePermission(Role.EntityName.USER, "delete"));
    }

    @Test
    void validatePermission_deniesPermission() throws AuthException {
        User user = new User(UUID.randomUUID(), "user", "hash", "email", Role.GENERAL);
        when(userRepository.findByUsername("user")).thenReturn(List.of(user));
        when(passwordService.verify(anyString(), anyString())).thenReturn(true);

        authService.login("user", "pass");

        AuthException ex =
                assertThrows(
                        AuthException.class,
                        () -> authService.validatePermission(Role.EntityName.ITEM, "delete"));
        assertTrue(ex.getMessage().contains("Required delete permission"));
    }
}
