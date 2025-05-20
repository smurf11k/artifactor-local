package com.renata.infrastructure.file.exception;

import java.io.Serial;

/** Виняток для обробки помилок під час роботи зі зберіганням файлів. */
public class FileStorageException extends RuntimeException {

    @Serial private static final long serialVersionUID = 1L;

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
