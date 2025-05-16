package com.renata.domain.entities;

import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import java.util.Objects;
import java.util.UUID;
import lombok.*;

/** Сутність, що представляє елемент антикваріату. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item implements Comparable<Item> {

    @EqualsAndHashCode.Include private UUID id;
    private String name;
    private AntiqueType type;
    private String description;
    private Integer productionYear;
    private String country;
    private ItemCondition condition;
    private String imagePath;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Item other) {
        int titleComparison = this.name.compareToIgnoreCase(other.name);
        if (titleComparison != 0) {
            return titleComparison;
        }
        return Integer.compare(other.productionYear, this.productionYear);
    }
}
