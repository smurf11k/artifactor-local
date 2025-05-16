package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
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
public class CollectionRepositoryImpl extends GenericRepository<Collection, UUID>
        implements CollectionRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public CollectionRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Collection.class, "collections");
    }

    /**
     * Пошук колекцій за ідентифікатором користувача.
     *
     * @param userId ідентифікатор користувача
     * @return список колекцій
     */
    @Override
    public List<Collection> findByUserId(UUID userId) {
        return findByField("user_id", userId);
    }

    /**
     * Пошук антикваріату у колекції за ідентифікатором колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return список антикваріату
     */
    @Override
    public List<Item> findItemsByCollectionId(UUID collectionId) {
        String baseSql =
                "SELECT a.* FROM items a JOIN item_collection ac ON a.id = ac.item_id WHERE"
                        + " ac.collection_id = ?";
        return executeQuery(
                baseSql, stmt -> stmt.setObject(1, collectionId), this::mapResultSetToItem);
    }

    /**
     * Пошук колекцій за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return список колекцій
     */
    @Override
    public List<Collection> findByItemId(UUID itemId) {
        String baseSql =
                "SELECT c.* FROM collections c JOIN item_collection ac ON c.id = ac.collection_id"
                        + " WHERE ac.item_id = ?";
        return executeQuery(
                baseSql, stmt -> stmt.setObject(1, itemId), this::mapResultSetToCollection);
    }

    /**
     * Прикріплення антикваріату до колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param itemId ідентифікатор антикваріату
     */
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

    /**
     * Від'єднання антикваріату від колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @param itemId ідентифікатор антикваріату
     */
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

    /**
     * Підрахунок антикваріату у колекції.
     *
     * @param collectionId ідентифікатор колекції
     * @return кількість антикваріату
     */
    @Override
    public long countItemsByCollectionId(UUID collectionId) {
        Filter filter =
                (whereClause, params) -> {
                    whereClause.add("collection_id = ?");
                    params.add(collectionId);
                };
        return count(filter, "item_collection");
    }

    /**
     * Пошук колекцій за назвою.
     *
     * @param name назва колекції
     * @return список колекцій
     */
    @Override
    public List<Collection> findByName(String name) {
        return findByField("name", name);
    }

    /**
     * Видалення всіх елементів антикваріату із колекції.
     *
     * @param collectionId ідентифікатор колекції
     */
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

    /**
     * Зіставлення ResultSet в антикваріат.
     *
     * @param rs результат запиту
     * @return антикваріат
     */
    private Item mapResultSetToItem(ResultSet rs) {
        try {
            Item item = new Item();
            item.setId(rs.getObject("id", UUID.class));
            item.setName(rs.getString("name"));
            item.setType(AntiqueType.valueOf(rs.getString("type")));
            item.setDescription(rs.getString("description"));
            item.setProductionYear(rs.getInt("year"));
            item.setCountry(rs.getString("country"));
            item.setCondition(ItemCondition.valueOf(rs.getString("condition")));
            item.setImagePath(rs.getString("image_path"));
            return item;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із антикваріату", e);
        }
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
