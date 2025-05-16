package com.renata.infrastructure.persistence.exception;

/** Виняток, що виникає, коли потрібна сутність не знайдена. */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
