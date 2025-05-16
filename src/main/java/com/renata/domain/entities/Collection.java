package com.renata.domain.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Сутність, що представляє колекцію антикваріату користувача. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Collection implements Comparable<Collection> {

    private UUID id;
    private UUID userId;
    private String name;
    private LocalDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Collection other) {
        int nameComparison = this.name.compareToIgnoreCase(other.name);
        if (nameComparison != 0) {
            return nameComparison;
        }
        return other.createdAt.compareTo(this.createdAt);
    }
}
