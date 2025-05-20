package com.renata.application.exception;

import java.io.Serial;

/** Виняток, що виникає при помилках автентифікації або авторизації. */
public class AuthException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }
}
