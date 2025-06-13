package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.MarketInfoRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Реалізація репозиторію для специфічних операцій з ринковою інформацією. */
@Repository
final class MarketInfoRepositoryImpl extends GenericRepository<MarketInfo, UUID>
        implements MarketInfoRepository {

    /**
     * Конструктор репозиторію.
     *
     * @param connectionPool пул з'єднань до бази даних
     */
    public MarketInfoRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, MarketInfo.class, "market_info");
    }

    /**
     * Пошук ринкової інформації за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return список ринкової інформації
     */
    @Override
    public List<MarketInfo> findByItemId(UUID itemId) {
        return findByField("item_id", itemId);
    }

    /**
     * Пошук ринкової інформації за типом події.
     *
     * @param type тип ринкової події
     * @return список ринкової інформації
     */
    @Override
    public List<MarketInfo> findByEventType(MarketEventType type) {
        return findByField("type", type.name());
    }

    /**
     * Пошук ринкової інформації за діапазоном дат.
     *
     * @param from початкова дата
     * @param to кінцева дата
     * @return список ринкової інформації
     */
    @Override
    public List<MarketInfo> findByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql =
                "SELECT * FROM market_info WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return executeQuery(
                sql,
                stmt -> {
                    stmt.setTimestamp(1, Timestamp.valueOf(from));
                    stmt.setTimestamp(2, Timestamp.valueOf(to));
                },
                this::mapResultSetToMarketInfo);
    }

    @Override
    public void deleteOlderThan(LocalDateTime olderThan) {
        String sql = "DELETE FROM market_info WHERE timestamp < ?";
        try (Connection conn = connectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(olderThan));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Error deleting old market info entries", e);
        }
    }

    /**
     * Зіставлення ResultSet в ринкову інформацію.
     *
     * @param rs результат запиту
     * @return ринкова інформація
     */
    private MarketInfo mapResultSetToMarketInfo(ResultSet rs) {
        try {
            MarketInfo marketInfo = new MarketInfo();
            marketInfo.setId(rs.getObject("id", UUID.class));
            marketInfo.setPrice(rs.getDouble("price"));
            marketInfo.setItemId(rs.getObject("item_id", UUID.class));
            marketInfo.setType(MarketEventType.valueOf(rs.getString("event_type")));
            Timestamp timestamp = rs.getTimestamp("timestamp");
            marketInfo.setTimestamp(timestamp != null ? timestamp.toLocalDateTime() : null);
            return marketInfo;
        } catch (Exception e) {
            throw new DatabaseAccessException("Error mapping ResultSet to MarketInfo", e);
        }
    }
}
