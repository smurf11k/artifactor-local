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
public class ItemRepositoryImpl extends GenericRepository<Item, UUID> implements ItemRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public ItemRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Item.class, "items");
    }

    /**
     * Пошук антикваріату за назвою.
     *
     * @param name назва
     * @return список антикваріату
     */
    @Override
    public List<Item> findByName(String name) {
        return findByField("name", name);
    }

    /**
     * Пошук антикваріату за типом.
     *
     * @param type тип антикваріату
     * @return список антикваріату
     */
    @Override
    public List<Item> findByType(AntiqueType type) {
        return findByField("type", type.name());
    }

    /**
     * Пошук антикваріату за країною походження.
     *
     * @param country країна
     * @return список антикваріату
     */
    @Override
    public List<Item> findByCountry(String country) {
        return findByField("country", country);
    }

    /**
     * Пошук антикваріату за його станом.
     *
     * @param condition стан антикваріату
     * @return список антикваріату
     */
    @Override
    public List<Item> findByCondition(ItemCondition condition) {
        return findByField("condition", condition.name());
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
