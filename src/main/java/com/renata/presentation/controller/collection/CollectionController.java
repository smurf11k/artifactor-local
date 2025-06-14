package com.renata.presentation.controller.collection;

import com.renata.application.contract.CollectionService;
import com.renata.application.dto.CollectionStoreDto;
import com.renata.application.dto.CollectionUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.presentation.viewmodel.CollectionViewModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для форми додавання та редагування колекції. */
@Component
public class CollectionController {

    @Autowired private CollectionService collectionService;
    @Autowired private Validator validator;

    @FXML private Label idLabel;
    @FXML private TextField nameField;
    @FXML private TextField userIdField;
    @FXML private TextField createdAtField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private CollectionViewModel collectionViewModel;
    private boolean isNewCollection = false;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        // Initialize fields and set createdAtField editable based on context
    }

    public void setNewCollection() {
        this.collectionViewModel = new CollectionViewModel(new Collection());
        this.isNewCollection = true;
        bindFieldsToViewModel();
        createdAtField.setEditable(true); // Allow editing for new collections
    }

    public void setCollection(Collection collection) {
        this.collectionViewModel = new CollectionViewModel(collection);
        this.isNewCollection = false;
        bindFieldsToViewModel();
        createdAtField.setEditable(false); // Read-only for editing existing collections
    }

    private void bindFieldsToViewModel() {
        idLabel.textProperty().bind(collectionViewModel.idProperty().asString());
        nameField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            collectionViewModel
                                    .nameProperty()
                                    .set(newValue != null ? newValue.trim() : null);
                        });
        nameField.setText(
                collectionViewModel.nameProperty().get() != null
                        ? collectionViewModel.nameProperty().get()
                        : "");
        userIdField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            try {
                                collectionViewModel
                                        .userIdProperty()
                                        .set(
                                                newValue != null && !newValue.trim().isEmpty()
                                                        ? UUID.fromString(newValue.trim())
                                                        : null);
                            } catch (IllegalArgumentException e) {
                                collectionViewModel.userIdProperty().set(null);
                            }
                        });
        userIdField.setText(
                collectionViewModel.userIdProperty().get() != null
                        ? collectionViewModel.userIdProperty().get().toString()
                        : "");
        createdAtField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            try {
                                LocalDateTime parsed =
                                        LocalDateTime.parse(newValue.trim(), DATE_TIME_FORMATTER);
                                collectionViewModel.createdAtProperty().set(parsed);
                            } catch (DateTimeParseException e) {
                                collectionViewModel.createdAtProperty().set(null);
                            }
                        });
        createdAtField.setText(
                collectionViewModel.createdAtProperty().get() != null
                        ? collectionViewModel.createdAtProperty().get().format(DATE_TIME_FORMATTER)
                        : "");
    }

    @FXML
    private void onSave() {
        try {
            if (collectionViewModel.nameProperty().get() == null
                    || collectionViewModel.nameProperty().get().trim().isEmpty()) {
                showErrorAlert(
                        "Помилка валідації",
                        "Назва колекції не може бути порожньою",
                        "Введіть коректну назву колекції.");
                return;
            }
            if (collectionViewModel.userIdProperty().get() == null) {
                showErrorAlert(
                        "Помилка валідації",
                        "ID користувача не може бути порожнім",
                        "Введіть коректний UUID для користувача.");
                return;
            }
            if (isNewCollection && collectionViewModel.createdAtProperty().get() == null) {
                showErrorAlert(
                        "Помилка валідації",
                        "Невірний формат часу створення",
                        "Введіть час у форматі yyyy-MM-dd HH:mm:ss.");
                return;
            }

            if (isNewCollection) {
                // Create new collection
                CollectionStoreDto collectionStoreDto =
                        new CollectionStoreDto(collectionViewModel.nameProperty().get());
                Set<ConstraintViolation<CollectionStoreDto>> createViolations =
                        validator.validate(collectionStoreDto);
                if (!createViolations.isEmpty()) {
                    throw ValidationException.create("collection creation", createViolations);
                }
                collectionService.create(collectionStoreDto);
            } else {
                // Update existing collection
                CollectionUpdateDto collectionUpdateDto =
                        new CollectionUpdateDto(
                                collectionViewModel.idProperty().get(),
                                collectionViewModel.userIdProperty().get(),
                                collectionViewModel.nameProperty().get());
                Set<ConstraintViolation<CollectionUpdateDto>> updateViolations =
                        validator.validate(collectionUpdateDto);
                if (!updateViolations.isEmpty()) {
                    throw ValidationException.create("collection update", updateViolations);
                }
                collectionService.update(
                        collectionViewModel.idProperty().get(), collectionUpdateDto);
            }

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();

            showInfoAlert("Успіх", "Колекцію успішно збережено", collectionViewModel.toString());

        } catch (ValidationException e) {
            showErrorAlert(
                    "Помилка валідації",
                    "Неправильні введені дані",
                    String.join("; ", e.getMessage()));
        } catch (Exception e) {
            showErrorAlert("Помилка", "Не вийшло зберегти колекцію", "Помилка: " + e.getMessage());
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
