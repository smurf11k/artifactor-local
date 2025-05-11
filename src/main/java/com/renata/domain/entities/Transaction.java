package com.renata.domain.entities;

import com.renata.domain.enums.TransactionType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an antique transaction (purchase/sale).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction implements Comparable<Transaction> {

    @EqualsAndHashCode.Include
    private UUID id;
    private TransactionType type;
    private UUID userId;
    private UUID itemId;
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Transaction other) {
        // Primary sort: timestamp (newest first)
        int timeComparison = other.timestamp.compareTo(this.timestamp);
        if (timeComparison != 0) {
            return timeComparison;
        }

        // Secondary sort: transaction type
        return this.type.compareTo(other.type);
    }

    /**
     * Builder pattern implementation for fluent creation
     */
    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    @NoArgsConstructor
    public static class TransactionBuilder {
        private UUID id;
        private TransactionType type;
        private UUID userId;
        private UUID itemId;
        private LocalDateTime timestamp = LocalDateTime.now();

        public TransactionBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TransactionBuilder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public TransactionBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public TransactionBuilder itemId(UUID itemId) {
            this.itemId = itemId;
            return this;
        }

        public TransactionBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Transaction build() {
            return new Transaction(id, type, userId, itemId, timestamp);
        }
    }
}