package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Реалізація репозиторію для специфічних операцій з антикваріатом. */
@Repository
final class ItemRepositoryImpl extends GenericRepository<Item, UUID> implements ItemRepository {

    public ItemRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Item.class, "items");
    }

    @Override
    public List<Item> findByName(String name) {
        return findByField("name", name);
    }

    @Override
    public List<Item> findByType(AntiqueType type) {
        return findByField("type", type.name());
    }

    @Override
    public List<Item> findByCountry(String country) {
        return findByField("country", country);
    }

    @Override
    public List<Item> findByCondition(ItemCondition condition) {
        return findByField("condition", condition.name());
    }

    @Override
    public List<Item> findItemsByCollectionId(UUID collectionId) {
        String baseSql =
                "SELECT a.* FROM items a JOIN item_collection ac ON a.id = ac.item_id WHERE"
                        + " ac.collection_id = ?";
        return executeQuery(
                baseSql, stmt -> stmt.setObject(1, collectionId), this::mapResultSetToItem);
    }

    private Item mapResultSetToItem(ResultSet rs) {
        try {
            Item item = new Item();
            item.setId(rs.getObject("id", UUID.class));
            item.setName(rs.getString("name"));
            item.setType(AntiqueType.valueOf(rs.getString("type")));
            item.setDescription(rs.getString("description"));
            item.setProductionYear(rs.getString("production_year"));
            item.setCountry(rs.getString("country"));
            item.setCondition(ItemCondition.valueOf(rs.getString("condition")));
            item.setImagePath(rs.getString("image_path"));
            return item;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet із антикваріату", e);
        }
    }
}
