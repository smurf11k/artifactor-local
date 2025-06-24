package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.renata.application.contract.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordServiceImplTest {

    PasswordService service;

    @BeforeEach
    void setup() {
        service = new PasswordServiceImpl();
    }

    @Test
    void hash_and_verify_workCorrectly() {
        String plainPassword = "mySecret123!";

        String hashed = service.hash(plainPassword);
        assertNotNull(hashed);
        assertNotEquals(plainPassword, hashed);

        boolean verified = service.verify(plainPassword, hashed);
        assertTrue(verified);

        assertFalse(service.verify("wrongPassword", hashed));
    }
}
