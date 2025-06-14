package com.renata.presentation.controller.user;

import com.renata.application.contract.AuthService;
import com.renata.application.exception.AuthException;
import com.renata.presentation.controller.MainController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.springframework.stereotype.Component;

/**
 * Контролер форми входу в систему. Обробляє автентифікацію через AuthService та відображає
 * повідомлення про помилки.
 */
@Component
public class SignInController {

    private final AuthService authenticationService;
    private final MainController mainController;
    @FXML public PasswordField passwordField;
    @FXML public CheckBox rememberMeCheckBox;
    @FXML private TextField usernameField;

    public SignInController(AuthService authenticationService, MainController mainController) {
        this.authenticationService = authenticationService;
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        usernameField.setOnKeyPressed(
                event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        handleLogin();
                    }
                });
        passwordField.setOnKeyPressed(
                event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        handleLogin();
                    }
                });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean rememberMe = rememberMeCheckBox.isSelected(); // TODO: finish remember me logic

        try {
            boolean authenticated = authenticationService.login(username, password);

            if (authenticated) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Успіх");
                alert.setContentText("Вхід виконано успішно!");
                alert.showAndWait()
                        .ifPresent(
                                response -> {
                                    if (response == ButtonType.OK) {
                                        mainController.handleItemListSelection();
                                    }
                                });
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
