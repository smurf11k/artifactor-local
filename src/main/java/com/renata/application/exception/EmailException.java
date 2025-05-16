package com.renata.application.exception;

/** Виняток, що виникає при помилках, пов'язаних з електронною поштою. */
public class EmailException extends RuntimeException {
    public EmailException(String message) {
        super(message);
    }
}
