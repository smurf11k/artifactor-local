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

    private UUID id;
    private String name;
    private AntiqueType type;
    private String description;
    private String productionYear;
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
        return this.name.compareToIgnoreCase(other.name);
    }
}
