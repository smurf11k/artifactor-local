package com.renata.infrastructure.persistence.exception;

import java.io.Serial;

/** Виняток, що виникає, коли потрібна сутність не знайдена. */
public class EntityNotFoundException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
