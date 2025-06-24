package com.renata.presentation.controller.user;

import com.renata.application.contract.AuthService;
import com.renata.application.exception.AuthException;
import com.renata.presentation.controller.MainController;
import com.renata.presentation.util.MessageManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Контролер форми входу в систему. Обробляє автентифікацію через AuthService та відображає
 * повідомлення про помилки.
 */
@Component
public class SignInController {

    @Autowired private final AuthService authenticationService;
    @Autowired private final MainController mainController;
    @Autowired private final MessageManager messageManager;
    @FXML public PasswordField passwordField;
    @FXML private TextField usernameField;

    public SignInController(
            AuthService authenticationService,
            MainController mainController,
            MessageManager messageManager) {
        this.authenticationService = authenticationService;
        this.mainController = mainController;
        this.messageManager = messageManager;
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

        try {
            boolean authenticated = authenticationService.login(username, password);

            if (authenticated) {
                messageManager.showInfoAlert(
                        "Успіх", "Вхід виконано успішно!", "Ви успішно авторизувалися в системі.");
                mainController.handleItemListSelection();
                System.out.println("Успішний логін для користувача: " + username);
            } else {
                messageManager.showErrorAlert("Помилка", "Невірний логін або пароль.", "");
            }
        } catch (AuthException e) {
            messageManager.showErrorAlert("Помилка авторизація", "Помилка", e.getMessage());
        }
    }
}
