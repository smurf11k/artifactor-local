package com.renata.application.impl;

import com.renata.application.contract.EmailService;
import com.renata.application.contract.SignUpService;
import com.renata.application.contract.UserService;
import com.renata.application.dto.UserStoreDto;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class SignUpServiceImpl implements SignUpService {

    private final UserService userService;
    private final EmailService emailService;

    public SignUpServiceImpl(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @Override
    public void signUp(UserStoreDto userStoreDto, Supplier<String> waitForUserInput) {
        emailService.initiateVerification(userStoreDto.email());
        emailService.verifyCodeFromInput(userStoreDto.email(), waitForUserInput);
        userService.create(userStoreDto);
    }
}
