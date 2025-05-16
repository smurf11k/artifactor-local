package com.renata.application.exception;

import java.io.Serial;

/** Виняток, що виникає при відмові у доступі до ресурсу або дії. */
public class AccessDeniedException extends RuntimeException {

    @Serial private static final long serialVersionUID = 174948262083496647L;

    public AccessDeniedException(String message) {
        super(message);
    }

    public static AccessDeniedException bannedUser(String suffix) {
        return new AccessDeniedException("Ви не маєте права " + suffix + ".");
    }
}
