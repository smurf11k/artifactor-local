package com.renata.presentation.controller.item;

import com.renata.application.contract.ItemService;
import com.renata.application.dto.ItemStoreDto;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.presentation.viewmodel.ItemViewModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для форми створення предмета антикваріату. */
@Component
public class ItemController {

    @Autowired private ItemService itemService;

    @FXML private Label idLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField productionYearField;
    @FXML private ComboBox<AntiqueType> typeComboBox;
    @FXML private TextField countryField;
    @FXML private ComboBox<ItemCondition> conditionComboBox;
    @FXML private TextField imagePathField;

    private ItemViewModel itemViewModel;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(AntiqueType.values());
        typeComboBox.setValue(AntiqueType.ANTIQUE);

        conditionComboBox.getItems().addAll(ItemCondition.values());
        conditionComboBox.setValue(ItemCondition.GOOD);

        itemViewModel =
                new ItemViewModel(
                        UUID.randomUUID(),
                        "Antique Chair",
                        "A vintage wooden chair from the 18th century",
                        AntiqueType.ANTIQUE,
                        "unknown",
                        "France",
                        ItemCondition.GOOD,
                        null);

        bindFieldsToViewModel();
    }

    private void bindFieldsToViewModel() {
        idLabel.setText(itemViewModel.getId().toString());
        nameField.textProperty().bindBidirectional(itemViewModel.nameProperty());
        descriptionArea.textProperty().bindBidirectional(itemViewModel.descriptionProperty());
        typeComboBox.valueProperty().bindBidirectional(itemViewModel.typeProperty());
        productionYearField
                .textProperty()
                .bindBidirectional(itemViewModel.productionYearProperty());
        countryField.textProperty().bindBidirectional(itemViewModel.countryProperty());
        conditionComboBox.valueProperty().bindBidirectional(itemViewModel.conditionProperty());
        imagePathField.textProperty().bindBidirectional(itemViewModel.imagePathProperty());
    }

    @FXML
    private void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser
                .getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(null);
        if (selectedImageFile != null) {
            String imagePath = selectedImageFile.getAbsolutePath();
            itemViewModel.imagePathProperty().set(imagePath);
            imagePathField.setText(imagePath);
        }
    }

    @FXML
    private void onSave() {
        System.out.println("Saving Item Data: " + itemViewModel);

        ItemStoreDto item =
                new ItemStoreDto(
                        itemViewModel.getName().get(),
                        itemViewModel.getType().get(),
                        itemViewModel.getDescription().get(),
                        itemViewModel.getProductionYear().get(),
                        itemViewModel.getCountry().get(),
                        itemViewModel.getCondition().get(),
                        itemViewModel.getImagePath().get() != null
                                ? Paths.get(itemViewModel.getImagePath().get())
                                : null);

        try {
            FileInputStream imageStream = null;
            String imageName = null;
            if (selectedImageFile != null) {
                imageStream = new FileInputStream(selectedImageFile);
                imageName = selectedImageFile.getName();
            }

            itemService.create(item, imageStream, imageName);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Item Information");
            alert.setHeaderText("Item Saved Successfully");
            alert.setContentText(itemViewModel.toString());
            alert.showAndWait();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Save Item");
            alert.setContentText("Error with image file: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onCancel() {
        System.out.println("Operation Cancelled");
    }

    @FXML
    private void onViewList() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/com/renata/presentation/view/ItemListView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Item List");
            stage.show();

            Stage currentStage = (Stage) nameField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Item List");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
