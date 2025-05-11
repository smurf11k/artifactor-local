package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.UUID;

/**
 * Реалізація репозиторію для специфічних операцій з антикваріатом.
 */
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
     * Пошук антикваріату за діапазоном років.
     *
     * @param startYear початковий рік
     * @param endYear кінцевий рік
     * @return список антикваріату
     */
    @Override
    public List<Item> findByYearRange(Integer startYear, Integer endYear) {
        String sql = "SELECT * FROM items WHERE year BETWEEN ? AND ?";
        return executeQuery(sql, stmt -> {
            stmt.setInt(1, startYear);
            stmt.setInt(2, endYear);
        }, this::mapResultSetToItem);
    }

    /**
     * Пошук антикваріату старішого за заданий рік.
     *
     * @param productionYear заданий рік
     * @return список антикваріату
     */
    @Override
    public List<Item> findOlderThan(Integer productionYear) {
        String sql = "SELECT * FROM items WHERE production_year < ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, productionYear), this::mapResultSetToItem);
    }

    /**
     * Пошук антикваріату новішого за заданий рік.
     *
     * @param productionYear заданий рік
     * @return список антикваріату
     */
    @Override
    public List<Item> findNewerThan(Integer productionYear) {
        String sql = "SELECT * FROM items WHERE production_year > ?";
        return executeQuery(sql, stmt -> stmt.setInt(1, productionYear), this::mapResultSetToItem);
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
            item.setProductionYear(rs.getInt("production_year"));
            item.setCountry(rs.getString("country"));
            item.setCondition(ItemCondition.valueOf(rs.getString("condition")));
            item.setImagePath(rs.getString("image_path"));
            return item;
        } catch (Exception e) {
            throw new DatabaseAccessException("Error mapping ResultSet to Item", e);
        }
    }
}