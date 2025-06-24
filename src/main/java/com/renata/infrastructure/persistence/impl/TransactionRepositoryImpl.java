package com.renata.infrastructure.persistence.impl;

import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.persistence.GenericRepository;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Реалізація репозиторію для специфічних операцій з транзакціями. */
@Repository
final class TransactionRepositoryImpl extends GenericRepository<Transaction, UUID>
        implements TransactionRepository {

    public TransactionRepositoryImpl(ConnectionPool connectionPool) {
        super(connectionPool, Transaction.class, "transactions");
    }

    @Override
    public List<Transaction> findByUserId(UUID userId) {
        return findByField("user_id", userId);
    }

    @Override
    public List<Transaction> findByItemId(UUID itemId) {
        return findByField("item_id", itemId);
    }

    @Override
    public List<Transaction> findByType(TransactionType type) {
        return findByField("type", type.name());
    }

    @Override
    public List<Transaction> findByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql =
                "SELECT * FROM transactions WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp"
                        + " DESC";
        return executeQuery(
                sql,
                stmt -> {
                    stmt.setTimestamp(1, Timestamp.valueOf(from));
                    stmt.setTimestamp(2, Timestamp.valueOf(to));
                },
                this::mapResultSetToTransaction);
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) {
        try {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getObject("id", UUID.class));
            transaction.setType(TransactionType.valueOf(rs.getString("type")));
            transaction.setUserId(rs.getObject("user_id", UUID.class));
            transaction.setItemId(rs.getObject("item_id", UUID.class));

            Timestamp timestamp = rs.getTimestamp("timestamp");
            transaction.setTimestamp(timestamp != null ? timestamp.toLocalDateTime() : null);

            return transaction;
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зіставлення ResultSet з транзакції", e);
        }
    }
}
