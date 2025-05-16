package com.renata.application.contract;

import com.renata.application.exception.AuthException;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;

/** Сервіс для управління автентифікацією та авторизацією користувачів. */
public interface AuthService {
    /**
     * Виконує вхід користувача в систему.
     *
     * @param username логін користувача
     * @param password пароль користувача
     * @return true, якщо автентифікація успішна
     * @throws AuthException якщо виникають помилки автентифікації
     */
    boolean login(String username, String password) throws AuthException;

    /**
     * Виконує вихід поточного користувача з системи.
     *
     * @throws AuthException якщо виникають помилки під час виходу
     */
    void logout() throws AuthException;

    /**
     * Отримує поточного авторизованого користувача.
     *
     * @return об'єкт авторизованого користувача
     * @throws AuthException якщо користувач не авторизований
     */
    User getCurrentUser() throws AuthException;

    /**
     * Перевіряє, чи є користувач авторизованим.
     *
     * @return true, якщо користувач авторизований
     */
    boolean isAuthenticated();

    /**
     * Перевіряє наявність прав доступу для поточного користувача.
     *
     * @param entity сутність, для якої перевіряються права
     * @param action дія, яку потрібно перевірити
     * @return true, якщо користувач має необхідні права
     * @throws AuthException AuthException якщо виникають помилки авторизації
     */
    boolean hasPermission(Role.EntityName entity, String action) throws AuthException;

    /**
     * Валідує права доступу для поточного користувача.
     *
     * @param entity сутність, для якої перевіряються права
     * @param action дія, яку потрібно перевірити
     * @throws AuthException якщо користувач не має необхідних прав
     */
    void validatePermission(Role.EntityName entity, String action) throws AuthException;
}
