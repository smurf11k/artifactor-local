package com.renata.presentation.controller;

import static com.renata.presentation.Runner.springContext;

import com.renata.presentation.Runner;
import com.renata.presentation.util.SpringFXMLLoader;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.springframework.stereotype.Component;

/** Головний контролер додатку. */
@Component
public class MainController {

    @FXML private ToggleGroup toggleGroup = new ToggleGroup();
    @FXML private BorderPane root;

    @FXML
    public void initialize() {
        ToggleButton initialButton = (ToggleButton) toggleGroup.getToggles().getFirst();
        initialButton.setSelected(true);
    }

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

    // TODO: add a theme change button (switch), check Runner.java for the atlantafx primer light
    // and dark themes

    private void switchPage(String fxmlFile) {
        try {
            var fxmlLoader = new SpringFXMLLoader(springContext);
            Pane newPage = (Pane) fxmlLoader.load(Runner.class.getResource(fxmlFile));
            root.setCenter(newPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
