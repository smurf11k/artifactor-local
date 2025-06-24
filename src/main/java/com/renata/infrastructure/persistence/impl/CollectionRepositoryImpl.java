package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Collection;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.CollectionRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Реалізація репозиторію для специфічних операцій з колекціями. */
@Repository
final class CollectionRepositoryImpl extends GenericRepository<Collection, UUID>
        implements CollectionRepository {

    public CollectionRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Collection.class, "collections");
    }

    @Override
    public List<Collection> findByUserId(UUID userId) {
        return findByField("user_id", userId);
    }

    @Override
    public List<Collection> findByItemId(UUID itemId) {
        String baseSql =
                "SELECT c.* FROM collections c JOIN item_collection ac ON c.id = ac.collection_id"
                        + " WHERE ac.item_id = ?";
        return executeQuery(
                baseSql, stmt -> stmt.setObject(1, itemId), this::mapResultSetToCollection);
    }

    @Override
    public void attachItemToCollection(UUID collectionId, UUID itemId) {
        String sql = "INSERT INTO item_collection (collection_id, item_id) VALUES (?, ?)";
        try (Connection connection = connectionPool.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, collectionId);
            statement.setObject(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                    "Помилка прикріплення антикваріату до колекції: " + sql, e);
        }
    }

    @Override
    public void detachItemFromCollection(UUID collectionId, UUID itemId) {
        String sql = "DELETE FROM item_collection WHERE collection_id = ? AND item_id = ?";
        try (Connection connection = connectionPool.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, collectionId);
            statement.setObject(2, itemId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException(
                    "Помилка від'єднання антикваріату від колекції: " + sql, e);
        }
    }

    @Override
    public long countItemsByCollectionId(UUID collectionId) {
        Filter filter =
                (whereClause, params) -> {
                    whereClause.add("collection_id = ?");
                    params.add(collectionId);
                };
        return count(filter, "item_collection");
    }

    @Override
    public List<Collection> findByName(String name) {
        return findByField("name", name);
    }

    @Override
    public void clearCollection(UUID collectionId) {
        String sql = "DELETE FROM item_collection WHERE collection_id = ?";
        try (Connection connection = connectionPool.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, collectionId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка очищення колекції: " + sql, e);
        }
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
