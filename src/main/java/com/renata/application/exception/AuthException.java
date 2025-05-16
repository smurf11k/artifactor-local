package com.renata.application.exception;

/** Виняток, що виникає при помилках автентифікації або авторизації. */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
