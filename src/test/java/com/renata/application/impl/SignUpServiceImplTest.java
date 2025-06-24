package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.renata.application.contract.UserService;
import com.renata.application.dto.UserStoreDto;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.api.EmailSender;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class SignUpServiceImplTest {

    @Mock UserService userService;
    @Mock EmailSender emailSender;
    @InjectMocks SignUpServiceImpl signUpService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signUp_callsEmailVerificationAndCreatesUser() {
        UserStoreDto dto =
                new UserStoreDto("testuser", "testuser@example.com", "password123", Role.GENERAL);

        Supplier<String> fakeInputSupplier = mock(Supplier.class);
        when(fakeInputSupplier.get()).thenReturn("123456");

        signUpService.signUp(dto, fakeInputSupplier);

        verify(emailSender).initiateVerification(dto.email());
        verify(emailSender).verifyCodeFromInput(dto.email(), fakeInputSupplier);
        verify(userService).create(dto);
    }
}
