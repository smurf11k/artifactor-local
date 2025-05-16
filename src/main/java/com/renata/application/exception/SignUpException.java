package com.renata.application.exception;

/** Виняток, що виникає при помилках під час реєстрації користувача. */
public class SignUpException extends RuntimeException {
    public SignUpException(String message) {
        super(message);
    }
}
