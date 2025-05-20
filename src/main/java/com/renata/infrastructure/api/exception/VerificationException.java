package com.renata.infrastructure.api.exception;

import java.io.Serial;

/** Виняток, що виникає при помилках, пов'язаних з верифікацією електронної пошти. */
public class VerificationException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public VerificationException(String message) {
        super(message);
    }
}
