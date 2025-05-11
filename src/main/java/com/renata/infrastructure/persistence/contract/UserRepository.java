package com.renata.infrastructure.persistence.contract;

import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.infrastructure.persistence.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Інтерфейс репозиторію для специфічних операцій з користувачами.
 */
public interface UserRepository extends Repository<User, UUID> {

    /**
     * Пошук користувача за ім’ям користувача.
     *
     * @param username ім’я користувача
     * @return список користувачів
     */
    List<User> findByUsername(String username);

    /**
     * Пошук користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return список користувачів
     */
    List<User> findByEmail(String email);

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    List<Collection> findCollectionsByUserId(UUID userId);

    /**
     * Пошук користувачів за частковою відповідністю імені.
     *
     * @param partialUsername часткове ім’я користувача
     * @return список користувачів
     */
    List<User> findByPartialUsername(String partialUsername);

    /**
     * Підрахунок колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій
     */
    long countCollectionsByUserId(UUID userId);

    /**
     * Перевірка існування користувача за ім’ям.
     *
     * @param username ім’я користувача
     * @return true, якщо користувач існує
     */
    boolean existsByUsername(String username);

    /**
     * Перевірка існування користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return true, якщо користувач існує
     */
    boolean existsByEmail(String email);
}
