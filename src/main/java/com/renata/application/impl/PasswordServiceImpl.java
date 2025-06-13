package com.renata.application.impl;

import com.password4j.Password;
import com.renata.application.contract.PasswordService;
import org.springframework.stereotype.Service;

@Service
final class PasswordServiceImpl implements PasswordService {
    @Override
    public String hash(String plainPassword) {
        return Password.hash(plainPassword).withBcrypt().getResult();
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        return Password.check(plainPassword, hashedPassword).withBcrypt();
    }
}
