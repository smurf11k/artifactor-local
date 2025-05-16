package com.renata.application.exception;

/** Виняток, що виникає при помилках, пов'язаних з верифікацією електронної пошти. */
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}
