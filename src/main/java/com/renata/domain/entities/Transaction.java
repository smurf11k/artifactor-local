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

    public Transaction(
            UUID id, UUID userId, UUID itemId, TransactionType type, LocalDateTime timestamp) {
        System.out.println("Creating Transaction with arguments:");
        System.out.println("1. id: " + id);
        System.out.println("2. userId: " + userId);
        System.out.println("3. itemId: " + itemId);
        System.out.println("4. type: " + type);
        System.out.println("5. timestamp: " + timestamp);
    }

    @EqualsAndHashCode.Include private UUID id;
    private UUID userId;
    private UUID itemId;
    private TransactionType type;
    private LocalDateTime timestamp;

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
            return new Transaction(id, userId, itemId, type, timestamp);
        }
    }
}
