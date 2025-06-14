package com.renata.presentation.viewmodel;

import com.renata.domain.entities.Collection;
import java.time.LocalDateTime;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Getter;
import lombok.Setter;

/** ViewModel для роботи з колекціями. */
@Getter
@Setter
public class CollectionViewModel {

    private final ObjectProperty<UUID> id = new SimpleObjectProperty<>();
    private final ObjectProperty<UUID> userId = new SimpleObjectProperty<>();
    private final ObjectProperty<String> name = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    public CollectionViewModel(Collection collection) {
        this.id.set(collection.getId() != null ? collection.getId() : UUID.randomUUID());
        this.userId.set(collection.getUserId());
        this.name.set(collection.getName());
        this.createdAt.set(
                collection.getCreatedAt() != null
                        ? collection.getCreatedAt()
                        : LocalDateTime.now());
    }

    public ObjectProperty<UUID> idProperty() {
        return id;
    }

    public ObjectProperty<UUID> userIdProperty() {
        return userId;
    }

    public ObjectProperty<String> nameProperty() {
        return name;
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    public Collection toEntity() {
        Collection collection = new Collection();
        collection.setId(id.get() != null ? id.get() : UUID.randomUUID());
        collection.setUserId(userId.get());
        collection.setName(name.get());
        collection.setCreatedAt(createdAt.get() != null ? createdAt.get() : LocalDateTime.now());
        return collection;
    }

    @Override
    public String toString() {
        return "CollectionViewModel{"
                + "id="
                + id.get()
                + ", userId="
                + userId.get()
                + ", name="
                + name.get()
                + ", createdAt="
                + createdAt.get()
                + '}';
    }
}
