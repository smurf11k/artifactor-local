package com.renata.application.contract;

import java.util.function.Supplier;

/** Сервіс для верифікації електронної пошти через одноразові коди. */
public interface EmailService {

    /**
     * Ініціює процес верифікації email, генеруючи та надсилаючи код підтвердження.
     *
     * @param email адреса електронної пошти для верифікації
     */
    void initiateVerification(String email);

    /**
     * Перевіряє код підтвердження, отриманий від користувача.
     *
     * @param email адреса, для якої виконується верифікація
     * @param waitForUserInput постачальник коду підтвердження від користувача
     */
    void verifyCodeFromInput(String email, Supplier<String> waitForUserInput);
}
