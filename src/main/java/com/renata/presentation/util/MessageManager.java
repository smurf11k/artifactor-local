package com.renata.presentation.util;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/* Менеджер повідомлень типу Alert для UI */
@Component
public class MessageManager {
    private static final String LOGO_PATH = "/images/logo.png";

    private MessageManager() {}

    private void setAlertIcon(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream(LOGO_PATH)));
    }

    public void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        setAlertIcon(alert);
        alert.showAndWait();
    }

    public void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        setAlertIcon(alert);
        alert.showAndWait();
    }
}
