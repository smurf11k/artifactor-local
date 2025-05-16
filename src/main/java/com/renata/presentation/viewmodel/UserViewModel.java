package com.renata.presentation.viewmodel;

import com.renata.domain.entities.User.Role;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserViewModel {

    private final ObjectProperty<UUID> id = new SimpleObjectProperty<>();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();
    private final ObjectProperty<Role> role = new SimpleObjectProperty<>();

    public UserViewModel(UUID id, String username, String email, String password, Role role) {
        this.id.set(id);
        this.username.set(username);
        this.email.set(email);
        this.password.set(password);
        this.role.set(role);
    }

    public ObjectProperty<UUID> idProperty() {
        return id;
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public ObjectProperty<Role> roleProperty() {
        return role;
    }

    @Override
    public String toString() {
        return "UserViewModel{"
                + "id="
                + id.get()
                + ", username='"
                + username.get()
                + '\''
                + ", email='"
                + email.get()
                + '\''
                + ", password='"
                + password.get()
                + '\''
                + ", role="
                + role.get()
                + '}';
    }
}
