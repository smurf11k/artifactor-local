package com.renata.domain.entities;

import com.renata.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;

/** Сутність, що представляє транзакцію здійснену з антикваріатом. */
@Getter
@Setter
@NoArgsConstructor
public class Transaction implements Comparable<Transaction> {

    @EqualsAndHashCode.Include private UUID id;
    private UUID userId;
    private UUID itemId;
    private TransactionType type;
    private LocalDateTime timestamp;

    public Transaction(
            UUID id, UUID userId, UUID itemId, TransactionType type, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
        this.type = type;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Transaction other) {
        int timeComparison = other.timestamp.compareTo(this.timestamp);
        if (timeComparison != 0) {
            return timeComparison;
        }
        return this.type.compareTo(other.type);
    }

    @Override
    public String toString() {
        return "Transaction(id="
                + id
                + ", userId="
                + userId
                + ", itemId="
                + itemId
                + ", type="
                + type
                + ", timestamp="
                + timestamp
                + ")";
    }

    /** Builder для точнішого створення записів транзакцій */
    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    @NoArgsConstructor
    public static class TransactionBuilder {
        private UUID id;
        private UUID userId;
        private UUID itemId;
        private TransactionType type;
        private LocalDateTime timestamp = LocalDateTime.now();

        public TransactionBuilder id(UUID id) {
            this.id = id;
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

        public TransactionBuilder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public TransactionBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Transaction build() {
            if (id == null
                    || userId == null
                    || itemId == null
                    || type == null
                    || timestamp == null) {
                throw new IllegalStateException(
                        "All fields (id, userId, itemId, type, timestamp) must be set");
            }
            return new Transaction(id, userId, itemId, type, timestamp);
        }
    }
}
