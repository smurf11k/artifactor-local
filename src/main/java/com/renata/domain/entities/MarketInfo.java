package com.renata.domain.entities;

import com.renata.domain.enums.MarketEventType;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class MarketInfo implements Comparable<MarketInfo> {

    public MarketInfo(
            UUID id, double price, UUID itemId, MarketEventType type, LocalDateTime timestamp) {
        this.id = id;
        this.price = price;
        this.itemId = itemId;
        this.type = type;
        this.timestamp = timestamp;
    }

    @EqualsAndHashCode.Include private UUID id;
    private double price;
    private UUID itemId;
    private MarketEventType type;
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketInfo that = (MarketInfo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(MarketInfo other) {
        int timeComparison = other.timestamp.compareTo(this.timestamp);
        if (timeComparison != 0) {
            return timeComparison;
        }
        return this.type.compareTo(other.type);
    }

    public static MarketInfoBuilder builder() {
        return new MarketInfoBuilder();
    }

    @NoArgsConstructor
    public static class MarketInfoBuilder {
        private UUID id;
        private double price;
        private UUID itemId;
        private MarketEventType type;
        private LocalDateTime timestamp = LocalDateTime.now();

        public MarketInfoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public MarketInfoBuilder price(double price) {
            this.price = price;
            return this;
        }

        public MarketInfoBuilder itemId(UUID itemId) {
            this.itemId = itemId;
            return this;
        }

        public MarketInfoBuilder type(MarketEventType type) {
            this.type = type;
            return this;
        }

        public MarketInfoBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MarketInfo build() {
            return new MarketInfo(id, price, itemId, type, timestamp);
        }
    }
}
