package com.renata.application.impl;

import com.renata.application.contract.PasswordService;
import com.renata.application.contract.UserService;
import com.renata.application.dto.UserStoreDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.exception.EntityNotFoundException;
import jakarta.validation.Validator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final Validator validator;
    private final PasswordService passwordService;

    public UserServiceImpl(
            UserRepository userRepository, Validator validator, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.validator = validator;
        this.passwordService = passwordService;
    }

    @Override
    public User findById(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    public User findByUsername(String username) {
        List<User> users = userRepository.findByUsername(username);
        if (users.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return users.get(0);
    }

    @Override
    public User findByEmail(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users.isEmpty()) {
            throw new EntityNotFoundException("User not found");
        }
        return users.get(0);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByPartialUsername(String partialUsername) {
        return userRepository.findByPartialUsername(partialUsername);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User create(UserStoreDto userStoreDto) {
        var violations = validator.validate(userStoreDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("user creation", violations);
        }

        User user =
                new User(
                        UUID.randomUUID(),
                        userStoreDto.username(),
                        passwordService.hash(userStoreDto.password()),
                        userStoreDto.email(),
                        userStoreDto.role());

        return userRepository.save(user);
    }

    @Override
    public long countCollectionsByUserId(UUID userId) {
        return userRepository.countCollectionsByUserId(userId);
    }

    @Override
    public List<Collection> findCollectionsByUserId(UUID userId) {
        return userRepository.findCollectionsByUserId(userId);
    }
}
