package com.renata.presentation.viewmodel;

import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import java.time.LocalDateTime;
import java.util.UUID;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

/** ViewModel для роботи з інформацією про ринок. */
@Getter
@Setter
public class MarketInfoViewModel {

    private final ObjectProperty<UUID> id = new SimpleObjectProperty<>();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final ObjectProperty<UUID> itemId = new SimpleObjectProperty<>();
    private final ObjectProperty<MarketEventType> type = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> timestamp = new SimpleObjectProperty<>();

    public MarketInfoViewModel(MarketInfo marketInfo) {
        this.id.set(marketInfo.getId() != null ? marketInfo.getId() : UUID.randomUUID());
        this.price.set(marketInfo.getPrice());
        this.itemId.set(marketInfo.getItemId());
        this.type.set(marketInfo.getType());
        this.timestamp.set(
                marketInfo.getTimestamp() != null
                        ? marketInfo.getTimestamp()
                        : LocalDateTime.now());
    }

    public ObjectProperty<UUID> idProperty() {
        return id;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public ObjectProperty<UUID> itemIdProperty() {
        return itemId;
    }

    public ObjectProperty<MarketEventType> typeProperty() {
        return type;
    }

    public ObjectProperty<LocalDateTime> timestampProperty() {
        return timestamp;
    }

    public MarketInfo toEntity() {
        return MarketInfo.builder()
                .id(id.get())
                .price(price.get())
                .itemId(itemId.get())
                .type(type.get())
                .timestamp(timestamp.get())
                .build();
    }

    @Override
    public String toString() {
        return "MarketInfoViewModel{"
                + "id="
                + id.get()
                + ", price="
                + price.get()
                + ", itemId="
                + itemId.get()
                + ", type="
                + type.get()
                + ", timestamp="
                + timestamp.get()
                + '}';
    }
}
