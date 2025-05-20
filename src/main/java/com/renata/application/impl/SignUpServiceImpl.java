package com.renata.application.impl;

import com.renata.application.contract.SignUpService;
import com.renata.application.contract.UserService;
import com.renata.application.dto.UserStoreDto;
import com.renata.infrastructure.api.EmailSender;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class SignUpServiceImpl implements SignUpService {

    private final UserService userService;
    private final EmailSender emailSender;

    public SignUpServiceImpl(UserService userService, EmailSender emailSender) {
        this.userService = userService;
        this.emailSender = emailSender;
    }

    @Override
    public void signUp(UserStoreDto userStoreDto, Supplier<String> waitForUserInput) {
        emailSender.initiateVerification(userStoreDto.email());
        emailSender.verifyCodeFromInput(userStoreDto.email(), waitForUserInput);
        userService.create(userStoreDto);
    }
}
