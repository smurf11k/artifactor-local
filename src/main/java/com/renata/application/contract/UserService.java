package com.renata.application.contract;

import com.renata.application.dto.UserStoreDto;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import java.util.List;
import java.util.UUID;

/** Інтерфейс для роботи з користувачами системи. */
public interface UserService {
    /**
     * Знаходить користувача за ідентифікатором.
     *
     * @param id ідентифікатор користувача
     * @return знайдений користувач
     */
    User findById(UUID id);

    /**
     * Знаходить користувача за ім'ям
     *
     * @param username ім'я користувача для пошуку
     * @return знайдений користувач
     */
    User findByUsername(String username);

    /**
     * Знаходить користувача за електронною поштою.
     *
     * @param email електронна пошта для пошуку
     * @return знайдений користувач
     */
    User findByEmail(String email);

    /**
     * Отримує список всіх користувачів.
     *
     * @return список користувачів
     */
    List<User> findAll();

    /**
     * Знаходить користувачів за частиною імені користувача.
     *
     * @param partialUsername частина імені користувача для пошуку
     * @return список користувачів, що відповідають критерію
     */
    List<User> findByPartialUsername(String partialUsername);

    /**
     * Перевіряє існування користувача за ім'ям користувача.
     *
     * @param username ім'я користувача для перевірки
     * @return true, якщо користувач з таким ім'ям існує
     */
    boolean existsByUsername(String username);

    /**
     * Перевіряє існування користувача за електронною поштою.
     *
     * @param email електронна пошта для перевірки
     * @return true, якщо користувач з такою поштою існує
     */
    boolean existsByEmail(String email);

    /**
     * Створює нового користувача.
     *
     * @param userStoreDto DTO з даними для створення користувача
     * @return створений користувач
     */
    User create(UserStoreDto userStoreDto);

    /**
     * Отримує кількість колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій користувача
     */
    long countCollectionsByUserId(UUID userId);

    /**
     * Отримує колекції користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій користувача
     */
    List<Collection> findCollectionsByUserId(UUID userId);
}
