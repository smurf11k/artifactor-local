package com.renata.application.exception;

import java.io.Serial;

/** Виняток, що виникає при помилках під час реєстрації користувача. */
public class SignUpException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1L;

    public SignUpException(String message) {
        super(message);
    }
}
