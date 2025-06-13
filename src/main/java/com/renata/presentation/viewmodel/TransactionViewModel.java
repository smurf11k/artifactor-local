package com.renata.presentation.viewmodel;

import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

/** ViewModel для роботи з транзакціями. */
@Getter
@Setter
public class TransactionViewModel {

    private final ObjectProperty<UUID> id = new SimpleObjectProperty<>();
    private final ObjectProperty<UUID> userId = new SimpleObjectProperty<>();
    private final ObjectProperty<UUID> itemId = new SimpleObjectProperty<>();
    private final ObjectProperty<TransactionType> type = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();

    public TransactionViewModel(Transaction transaction) {
        this.id.set(transaction.getId() != null ? transaction.getId() : UUID.randomUUID());
        this.userId.set(transaction.getUserId());
        this.itemId.set(transaction.getItemId());
        this.type.set(
                transaction.getType() != null ? transaction.getType() : TransactionType.PURCHASE);
        this.timestamp.set(
                transaction.getTimestamp() != null
                        ? transaction.getTimestamp()
                        : LocalDateTime.now());
    }

    public ObjectProperty<UUID> idProperty() {
        return id;
    }

    public ObjectProperty<UUID> userIdProperty() {
        return userId;
    }

    public ObjectProperty<UUID> itemIdProperty() {
        return itemId;
    }

    public ObjectProperty<TransactionType> typeProperty() {
        return type;
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }

    public Transaction toEntity() {
        Transaction.TransactionBuilder builder =
                Transaction.builder()
                        .id(id.get() != null ? id.get() : UUID.randomUUID())
                        .type(type.get() != null ? type.get() : TransactionType.PURCHASE)
                        .timestamp(timestamp.get() != null ? timestamp.get() : LocalDateTime.now());

        if (userId.get() != null) {
            builder.userId(userId.get());
        }
        if (itemId.get() != null) {
            builder.itemId(itemId.get());
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return "TransactionViewModel{"
                + "id="
                + id.get()
                + ", userId="
                + userId.get()
                + ", itemId="
                + itemId.get()
                + ", type="
                + type.get()
                + ", timestamp="
                + timestamp.get()
                + '}';
    }
}
