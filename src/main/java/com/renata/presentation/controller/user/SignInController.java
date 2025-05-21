package com.renata.presentation.controller.user;

import com.renata.application.contract.AuthService;
import com.renata.application.exception.AuthException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

/**
 * Контролер форми входу в систему. Обробляє автентифікацію через AuthService та відображає
 * повідомлення про помилки.
 */
@Component
public class SignInController {

    private final AuthService authenticationService;
    @FXML public PasswordField passwordField;
    @FXML public CheckBox rememberMeCheckBox;
    @FXML private TextField usernameField;

    public SignInController(AuthService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @FXML
    public void initialize() {}

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckBox.isSelected();

        try {
            boolean authenticated = authenticationService.login(username, password);

            if (authenticated) {
                showAlert(Alert.AlertType.INFORMATION, "Успіх", "Вхід виконано успішно!");
                System.out.println("Login successful for: " + username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Невірний логін або пароль.");
            }
        } catch (AuthException e) {
            showAlert(Alert.AlertType.ERROR, "Помилка", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
