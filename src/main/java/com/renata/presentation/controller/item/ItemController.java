package com.renata.presentation.controller.item;

import com.renata.application.contract.ItemService;
import com.renata.application.dto.ItemStoreDto;
import com.renata.application.dto.ItemUpdateDto;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.presentation.viewmodel.ItemViewModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Контролер для форми створення та редагування предмета антикваріату. */
@Component
public class ItemController {

    @Autowired private ItemService itemService;
    @Autowired private ApplicationContext context;
    @Autowired private Validator validator;

    @FXML private Label idLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField productionYearField;
    @FXML private ComboBox<AntiqueType> typeComboBox;
    @FXML private TextField countryField;
    @FXML private ComboBox<ItemCondition> conditionComboBox;
    @FXML private TextField imagePathField;
    @FXML private HBox buttonBox;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private ItemViewModel itemViewModel;
    private File selectedImageFile;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(AntiqueType.values());
        conditionComboBox.getItems().addAll(ItemCondition.values());
    }

    public void initForNewItem() {
        isEditMode = false;
        itemViewModel =
                new ItemViewModel(
                        UUID.randomUUID(),
                        "",
                        "",
                        AntiqueType.ANTIQUE,
                        "",
                        "",
                        ItemCondition.GOOD,
                        null);
        selectedImageFile = null;
        bindFieldsToViewModel();
    }

    public void setItem(Item item) {
        isEditMode = true;
        itemViewModel =
                new ItemViewModel(
                        item.getId() != null ? item.getId() : UUID.randomUUID(),
                        item.getName() != null ? item.getName() : "",
                        item.getDescription() != null ? item.getDescription() : "",
                        item.getType() != null ? item.getType() : AntiqueType.ANTIQUE,
                        item.getProductionYear() != null ? item.getProductionYear() : "",
                        item.getCountry() != null ? item.getCountry() : "",
                        item.getCondition() != null ? item.getCondition() : ItemCondition.GOOD,
                        item.getImagePath());
        selectedImageFile = item.getImagePath() != null ? new File(item.getImagePath()) : null;
        bindFieldsToViewModel();
    }

    private void bindFieldsToViewModel() {
        idLabel.textProperty().bind(itemViewModel.idProperty().asString());
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
        try {
            String imageName = null;
            Path imagePath = null;

            if (selectedImageFile != null) {
                imageName = selectedImageFile.getName();
                imagePath = Paths.get(selectedImageFile.getAbsolutePath());
            }

            if (isEditMode) {
                ItemUpdateDto itemUpdateDto =
                        new ItemUpdateDto(
                                itemViewModel.getId().get(),
                                itemViewModel.getName().get(),
                                itemViewModel.getType().get(),
                                itemViewModel.getDescription().get(),
                                itemViewModel.getProductionYear().get(),
                                itemViewModel.getCountry().get(),
                                itemViewModel.getCondition().get(),
                                imagePath);

                Set<ConstraintViolation<ItemUpdateDto>> violations =
                        validator.validate(itemUpdateDto);
                if (!violations.isEmpty()) {
                    String errorMessage =
                            violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining("; "));
                    showErrorAlert("Помилка валідації", "Неправильні введені дані", errorMessage);
                    return;
                }

                if (selectedImageFile != null) {
                    try (FileInputStream imageStream = new FileInputStream(selectedImageFile)) {
                        itemService.update(
                                itemViewModel.getId().get(), itemUpdateDto, imageStream, imageName);
                    }
                } else {
                    itemService.update(itemViewModel.getId().get(), itemUpdateDto, null, null);
                }
            } else {
                ItemStoreDto itemStoreDto =
                        new ItemStoreDto(
                                itemViewModel.getName().get(),
                                itemViewModel.getType().get(),
                                itemViewModel.getDescription().get(),
                                itemViewModel.getProductionYear().get(),
                                itemViewModel.getCountry().get(),
                                itemViewModel.getCondition().get(),
                                imagePath);

                Set<ConstraintViolation<ItemStoreDto>> violations =
                        validator.validate(itemStoreDto);
                if (!violations.isEmpty()) {
                    String errorMessage =
                            violations.stream()
                                    .map(ConstraintViolation::getMessage)
                                    .collect(Collectors.joining("; "));
                    showErrorAlert("Помилка валідації", "Неправильні введені дані", errorMessage);
                    return;
                }

                if (selectedImageFile != null) {
                    try (FileInputStream imageStream = new FileInputStream(selectedImageFile)) {
                        itemService.create(itemStoreDto, imageStream, imageName);
                    }
                } else {
                    itemService.create(itemStoreDto, null, null);
                }
            }

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();

            showInfoAlert(
                    "Успіх",
                    isEditMode ? "Предмет успішно оновлено" : "Предмет успішно збережено",
                    itemViewModel.toString());

        } catch (IOException e) {
            showErrorAlert(
                    "Помилка",
                    "Не вийшло зберегти предмет",
                    "Помилка із зображенням предмету: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
