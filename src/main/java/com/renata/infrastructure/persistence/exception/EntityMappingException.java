package com.renata.infrastructure.persistence.exception;

import java.io.Serial;

/** Виняток, що виникає при помилках зіставлення сутності з даними бази. */
public class EntityMappingException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public EntityMappingException(String message) {
        super(message);
    }

    public EntityMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
