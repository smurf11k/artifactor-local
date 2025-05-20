package com.renata.presentation.viewmodel;

import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

/** ViewModel для роботи з предметами антикваріату. */
@Getter
@Setter
public class ItemViewModel {

    private final ObjectProperty<UUID> id = new SimpleObjectProperty<>();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<AntiqueType> type = new SimpleObjectProperty<>();
    private final StringProperty productionYear = new SimpleStringProperty();
    private final StringProperty country = new SimpleStringProperty();
    private final ObjectProperty<ItemCondition> condition = new SimpleObjectProperty<>();
    private final StringProperty imagePath = new SimpleStringProperty();

    public ItemViewModel(
            UUID id,
            String name,
            String description,
            AntiqueType type,
            String productionYear,
            String country,
            ItemCondition condition,
            String imagePath) {
        this.id.set(id);
        this.name.set(name);
        this.description.set(description);
        this.type.set(type);
        this.productionYear.set(productionYear);
        this.country.set(country);
        this.condition.set(condition);
        this.imagePath.set(imagePath);
    }

    public ObjectProperty<UUID> idProperty() {
        return id;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<AntiqueType> typeProperty() {
        return type;
    }

    public StringProperty productionYearProperty() {
        return productionYear;
    }

    public StringProperty countryProperty() {
        return country;
    }

    public ObjectProperty<ItemCondition> conditionProperty() {
        return condition;
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    @Override
    public String toString() {
        return "ItemViewModel{"
                + "id="
                + id.get()
                + ", name='"
                + name.get()
                + '\''
                + ", description='"
                + description.get()
                + '\''
                + ", type="
                + type.get()
                + ", productionYear='"
                + productionYear.get()
                + '\''
                + ", country='"
                + country.get()
                + '\''
                + ", condition="
                + condition.get()
                + ", imagePath='"
                + imagePath.get()
                + '\''
                + '}';
    }
}
