package com.renata.application.impl;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.PasswordService;
import com.renata.application.exception.AuthException;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.persistence.contract.UserRepository;
import org.springframework.stereotype.Service;

@Service
final class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private User currentUser;

    public AuthServiceImpl(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public boolean login(String username, String password) throws AuthException {
        if (currentUser != null) {
            throw new AuthException("Ви вже авторизовані як: " + currentUser.getUsername());
        }

        User user =
                userRepository.findByUsername(username).stream()
                        .findFirst()
                        .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!passwordService.verify(password, user.getPasswordHash())) {
            return false;
        }

        currentUser = user;
        System.out.println("User logged in: " + username + ", ID: " + user.getId());
        return true;
    }

    @Override
    public void logout() throws AuthException {
        if (!isAuthenticated()) {
            throw new AuthException("No active session");
        }
        currentUser = null;
    }

    @Override
    public User getCurrentUser() throws AuthException {
        if (!isAuthenticated()) {
            throw new AuthException("Not authenticated");
        }
        return currentUser;
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public boolean hasPermission(Role.EntityName entity, String action) throws AuthException {
        Role role = getCurrentUser().getRole();
        Role.Permission permission = role.getPermissions().get(entity);

        if (permission == null) {
            return false;
        }

        return switch (action.toLowerCase()) {
            case "create" -> permission.canAdd();
            case "read" -> permission.canRead();
            case "update" -> permission.canEdit();
            case "delete" -> permission.canDelete();
            default -> false;
        };
    }

    @Override
    public void validatePermission(Role.EntityName entity, String action) throws AuthException {
        if (!hasPermission(entity, action)) {
            throw new AuthException(
                    String.format("Required %s permission for %s not granted", action, entity));
        }
    }
}
