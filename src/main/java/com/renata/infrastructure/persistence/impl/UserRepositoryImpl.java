package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з користувачами.
 */
@Repository
public class UserRepositoryImpl extends GenericRepository<User, UUID> implements UserRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public UserRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, User.class, "users");
    }

    /**
     * Пошук користувача за ім’ям користувача.
     *
     * @param username ім’я користувача
     * @return список користувачів
     */
    @Override
    public List<User> findByUsername(String username) {
        return findByField("username", username);
    }

    /**
     * Пошук користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return список користувачів
     */
    @Override
    public List<User> findByEmail(String email) {
        return findByField("email", email);
    }

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    @Override
    public List<Collection> findCollectionsByUserId(UUID userId) {
        String baseSql = "SELECT * FROM collections WHERE user_id = ?";
        return executeQuery(baseSql, stmt -> stmt.setObject(1, userId), this::mapResultSetToCollection);
    }

    /**
     * Пошук користувачів за частковою відповідністю імені.
     *
     * @param partialUsername часткове ім’я користувача
     * @return список користувачів
     */
    @Override
    public List<User> findByPartialUsername(String partialUsername) {
        return findAll(
            (whereClause, params) -> {
                whereClause.add("username LIKE ?"); //ILIKE ????
                params.add("%" + partialUsername + "%");
            },
            null, true, 0, Integer.MAX_VALUE
        );
    }

    /**
     * Підрахунок колекцій користувача.
     *
     * @param userId ідентифікатор користувача
     * @return кількість колекцій
     */
    @Override
    public long countCollectionsByUserId(UUID userId) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("user_id = ?");
            params.add(userId);
        };
        return count(filter, "collections");
    }

    /**
     * Перевірка існування користувача за ім’ям.
     *
     * @param username ім’я користувача
     * @return true, якщо користувач існує
     */
    @Override
    public boolean existsByUsername(String username) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("username = ?");
            params.add(username);
        };
        return count(filter) > 0;
    }

    /**
     * Перевірка існування користувача за електронною поштою.
     *
     * @param email електронна пошта
     * @return true, якщо користувач існує
     */
    @Override
    public boolean existsByEmail(String email) {
        Filter filter = (whereClause, params) -> {
            whereClause.add("email = ?");
            params.add(email);
        };
        return count(filter) > 0;
    }

    /**
     * Зіставлення ResultSet у колекцію.
     *
     * @param rs результат запиту
     * @return колекція
     */
    private Collection mapResultSetToCollection(ResultSet rs) {
        try {
            Collection collection = new Collection();
            collection.setId(rs.getObject("id", UUID.class));
            collection.setUserId(rs.getObject("user_id", UUID.class));
            collection.setName(rs.getString("name"));
            Timestamp createdAt = rs.getTimestamp("created_at");
            collection.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
            return collection;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із колекцією", e);
        }
    }


}
