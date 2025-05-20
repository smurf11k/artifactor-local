package com.renata.infrastructure.persistence.exception;

import java.io.Serial;

/** Виняток, що виникає при помилках доступу до бази даних. */
public class DatabaseAccessException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public DatabaseAccessException(String message) {
        super(message);
    }

    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
