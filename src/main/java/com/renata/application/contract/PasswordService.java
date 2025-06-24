package com.renata.application.contract;

/** Сервіс для роботи з паролями. */
public interface PasswordService {
    /**
     * Генерує хеш пароля.
     *
     * @param plainPassword пароль у відкритій формі
     * @return хешований пароль
     */
    String hash(String plainPassword);

    /**
     * Перевіряє відповідність пароля його хешу.
     *
     * @param plainPassword пароль у відкритій формі для перевірки
     * @param hashedPassword збережений хеш пароля для порівняння
     * @return true, якщо пароль відповідає хешу
     */
    boolean verify(String plainPassword, String hashedPassword);
}
