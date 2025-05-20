package com.renata.infrastructure.api.exception;

import java.io.Serial;

/** Виняток, що виникає при помилках, пов'язаних з електронною поштою. */
public class EmailException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public EmailException(String message) {
        super(message);
    }
}
