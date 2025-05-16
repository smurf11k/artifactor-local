package com.renata.presentation.controller.user;

import com.renata.application.contract.UserService;
import com.renata.application.dto.UserStoreDto;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.file.FileStorageService;
import com.renata.presentation.viewmodel.UserViewModel;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignUpController {

    @Autowired private UserService userService;
    @Autowired private FileStorageService fileService;
    @FXML private Label idLabel;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;

    private UserViewModel userViewModel;

    @FXML
    public void initialize() {
        // Ініціалізація ролей у ComboBox
        roleComboBox.getItems().addAll(Role.values());
        roleComboBox.setValue(Role.GENERAL);

        // Створення користувача з пустими даними як приклад
        userViewModel =
                new UserViewModel(
                        UUID.randomUUID(),
                        "JohnDoe",
                        "john.doe@example.com",
                        "password123",
                        Role.GENERAL);

        // Зв'язування властивостей ViewModel з View
        bindFieldsToViewModel();
    }

    private void bindFieldsToViewModel() {
        idLabel.setText(userViewModel.getId().toString());
        usernameField.textProperty().bindBidirectional(userViewModel.usernameProperty());
        emailField.textProperty().bindBidirectional(userViewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(userViewModel.passwordProperty());
        roleComboBox.valueProperty().bindBidirectional(userViewModel.roleProperty());
    }

    @FXML
    private void onSave() {
        System.out.println("Saving User Data: " + userViewModel);

        // Відображення інформації через Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Information");
        alert.setHeaderText("User Data Saved Successfully");
        alert.setContentText(userViewModel.toString());
        alert.showAndWait();

        UserStoreDto userStoreDto =
                new UserStoreDto(
                        userViewModel.getUsername().get(),
                        userViewModel.getPassword().get(),
                        userViewModel.getEmail().get(),
                        userViewModel.getRole().get());
        userService.create(userStoreDto);
    }

    @FXML
    private void onCancel() {
        System.out.println("Operation Cancelled");
    }
}
