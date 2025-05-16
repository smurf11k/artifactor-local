package com.renata.application.impl;

import com.password4j.Password;
import com.renata.application.contract.PasswordService;
import org.springframework.stereotype.Service;

@Service
public class PasswordServiceImpl implements PasswordService {
    @Override
    public String hash(String plainPassword) {
        String hash = Password.hash(plainPassword).withBcrypt().getResult();
        System.out.println("Generated bcrypt hash: " + hash);
        return hash;
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        System.out.println("Verifying against hash: " + hashedPassword);
        return Password.check(plainPassword, hashedPassword).withBcrypt();
    }
}
