package com.renata.application.contract;

import com.renata.application.dto.UserStoreDto;
import java.util.function.Supplier;

/** Сервіс для реєстрації нових користувачів. */
public interface SignUpService {
    /**
     * Виконує процес реєстрації нового користувача з підтвердженням.
     *
     * @param userStoreDto DTO з даними для створення користувача
     * @param waitForUserInput постачальник для отримання коду підтвердження
     */
    void signUp(UserStoreDto userStoreDto, Supplier<String> waitForUserInput);
}
