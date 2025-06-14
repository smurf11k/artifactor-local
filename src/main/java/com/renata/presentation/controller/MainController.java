package com.renata.presentation.controller;

import static com.renata.presentation.Runner.springContext;

import com.renata.application.contract.AuthService;
import com.renata.application.exception.AuthException;
import com.renata.domain.entities.User;
import com.renata.presentation.Runner;
import com.renata.presentation.util.SpringFXMLLoader;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

/** Головний контролер додатку. */
@Component
public class MainController {

    @FXML private ToggleGroup toggleGroup = new ToggleGroup();
    @FXML private BorderPane root;
    @FXML private VBox menuPane;
    @FXML private VBox userInfoPane;
    @FXML private VBox menuButtonsPane;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private ToggleButton signInButton;
    @FXML private ToggleButton signUpButton;
    @FXML private Hyperlink logoutLink;

    private final AuthService authenticationService;

    public MainController(AuthService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @FXML
    public void initialize() {
        signInButton.setSelected(true);
        updateMenuVisibility();
    }

    // TODO: add role protection to every page except sign in and sign up
    @FXML
    private void handleMenuSelection(ActionEvent actionEvent) {
        ToggleButton selectedButton = (ToggleButton) toggleGroup.getSelectedToggle();
        if (selectedButton != null) {
            switch (selectedButton.getText()) {
                case "Авторизація" -> switchPage("/com/renata/view/user/SignIn.fxml");
                case "Реєстрація" -> switchPage("/com/renata/view/user/SignUp.fxml");
                case "Антикваріат" -> switchPage("/com/renata/view/item/ItemList.fxml");
                case "Транзакції" ->
                        switchPage("/com/renata/view/transaction/TransactionList.fxml");
                case "Колекції" -> switchPage("/com/renata/view/collection/CollectionList.fxml");
                case "Ринок" -> switchPage("/com/renata/view/market/Market.fxml");
                default ->
                        System.err.println(
                                String.format("Unknown selection: %s", selectedButton.getText()));
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            authenticationService.logout();
            switchPage("/com/renata/view/user/SignIn.fxml");
            updateMenuVisibility();
            signInButton.setSelected(true);
        } catch (AuthException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка");
            alert.setContentText("Помилка під час виходу: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void switchPage(String fxmlFile) {
        try {
            var fxmlLoader = new SpringFXMLLoader(springContext);
            Pane newPage = (Pane) fxmlLoader.load(Runner.class.getResource(fxmlFile));
            root.setCenter(newPage);
            updateMenuVisibility();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleItemListSelection() {
        toggleGroup.getToggles().stream()
                .map(toggle -> (ToggleButton) toggle)
                .filter(button -> button.getText().equals("Антикваріат"))
                .findFirst()
                .ifPresent(
                        button -> {
                            button.setSelected(true);
                            handleMenuSelection(new ActionEvent(button, null));
                        });
    }

    public void updateMenuVisibility() {
        if (authenticationService.isAuthenticated()) {
            User user = authenticationService.getCurrentUser();
            if (user != null) {
                usernameLabel.setText(user.getUsername());
                roleLabel.setText(user.getRole().toString());
                emailLabel.setText(user.getEmail());
                userInfoPane.setVisible(true);
                userInfoPane.setManaged(true);
                menuButtonsPane.setVisible(false);
                menuButtonsPane.setManaged(false);
            }
        } else {
            userInfoPane.setVisible(false);
            userInfoPane.setManaged(false);
            menuButtonsPane.setVisible(true);
            menuButtonsPane.setManaged(true);
        }
    }
}
