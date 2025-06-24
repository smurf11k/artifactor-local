package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Реалізація репозиторію для специфічних операцій з користувачами. */
@Repository
final class UserRepositoryImpl extends GenericRepository<User, UUID> implements UserRepository {

    public UserRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, User.class, "users");
    }

    @Override
    public List<User> findByUsername(String username) {
        return findByField("username", username);
    }

    @Override
    public List<User> findByEmail(String email) {
        return findByField("email", email);
    }

    @Override
    public List<Collection> findCollectionsByUserId(UUID userId) {
        String baseSql = "SELECT * FROM collections WHERE user_id = ? ORDER BY created_at";
        return executeQuery(
                baseSql, stmt -> stmt.setObject(1, userId), this::mapResultSetToCollection);
    }

    @Override
    public List<User> findByPartialUsername(String partialUsername) {
        return findAll(
                (whereClause, params) -> {
                    whereClause.add("username ILIKE ?");
                    params.add("%" + partialUsername + "%");
                },
                null,
                true,
                0,
                Integer.MAX_VALUE);
    }

    @Override
    public long countCollectionsByUserId(UUID userId) {
        Filter filter =
                (whereClause, params) -> {
                    whereClause.add("user_id = ?");
                    params.add(userId);
                };
        return count(filter, "collections");
    }

    @Override
    public boolean existsByUsername(String username) {
        Filter filter =
                (whereClause, params) -> {
                    whereClause.add("username = ?");
                    params.add(username);
                };
        return count(filter) > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Filter filter =
                (whereClause, params) -> {
                    whereClause.add("email = ?");
                    params.add(email);
                };
        return count(filter) > 0;
    }

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
