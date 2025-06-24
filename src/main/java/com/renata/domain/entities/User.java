package com.renata.domain.entities;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Сутність, що представляє користувача системи. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Comparable<User> {

    private UUID id;
    private String username;
    private String passwordHash;
    private String email;
    private Role role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(User other) {
        return this.username.compareToIgnoreCase(other.username);
    }

    @Getter
    public enum Role {
        ADMIN(
                "admin",
                Map.of(
                        EntityName.ITEM, new Permission(true, true, true, true),
                        EntityName.COLLECTION, new Permission(true, true, true, true),
                        EntityName.TRANSACTION, new Permission(true, true, true, true),
                        EntityName.MARKET, new Permission(true, true, true, true),
                        EntityName.USER, new Permission(true, true, true, true))),
        GENERAL(
                "general",
                Map.of(
                        EntityName.ITEM, new Permission(false, false, false, true),
                        EntityName.COLLECTION, new Permission(true, true, true, true),
                        EntityName.TRANSACTION, new Permission(true, false, false, true),
                        EntityName.MARKET, new Permission(false, false, false, true),
                        EntityName.USER, new Permission(true, false, false, true)));

        private final String name;
        private final Map<EntityName, Permission> permissions;

        Role(String name, Map<EntityName, Permission> permissions) {
            this.name = name;
            this.permissions = permissions;
        }

        public enum EntityName {
            ITEM,
            COLLECTION,
            TRANSACTION,
            MARKET,
            USER
        }

        public record Permission(
                boolean canAdd, boolean canEdit, boolean canDelete, boolean canRead) {}
    }
}
