package com.renata.presentation.controller.collection;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.CollectionService;
import com.renata.application.dto.CollectionStoreDto;
import com.renata.application.dto.CollectionUpdateDto;
import com.renata.application.exception.AuthException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Collection;
import com.renata.presentation.util.MessageManager;
import com.renata.presentation.viewmodel.CollectionViewModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для форми додавання та редагування колекції. */
@Component
public class CollectionController {

    @Autowired private CollectionService collectionService;
    @Autowired private Validator validator;
    @Autowired private AuthService authService;
    @Autowired private MessageManager messageManager;

    @FXML private Label idLabel;
    @FXML private TextField nameField;
    @FXML private TextField userIdField;
    @FXML private TextField createdAtField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private HBox buttonBox;

    private CollectionViewModel collectionViewModel;
    private boolean isNewCollection = false;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        userIdField.setEditable(false);
    }

    public void setNewCollection() {
        this.collectionViewModel = new CollectionViewModel(new Collection());
        this.isNewCollection = true;
        bindFieldsToViewModel();
        createdAtField.setEditable(true);
        setCurrentUserId();
    }

    public void setCollection(Collection collection) {
        this.collectionViewModel = new CollectionViewModel(collection);
        this.isNewCollection = false;
        bindFieldsToViewModel();
        createdAtField.setEditable(false);
        setCurrentUserId();
    }

    private void setCurrentUserId() {
        try {
            UUID userId = authService.getCurrentUser().getId();
            collectionViewModel.userIdProperty().set(userId);
            userIdField.setText(userId != null ? userId.toString() : "");
        } catch (AuthException e) {
            messageManager.showErrorAlert(
                    "Помилка автентифікації",
                    "Не вдалося отримати ID користувача",
                    "Помилка: " + e.getMessage());
        }
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
                        : LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    @FXML
    private void onSave() {
        try {
            if (isNewCollection) {
                CollectionStoreDto collectionStoreDto =
                        new CollectionStoreDto(
                                collectionViewModel.userIdProperty().get(),
                                collectionViewModel.nameProperty().get());
                Set<ConstraintViolation<CollectionStoreDto>> createViolations =
                        validator.validate(collectionStoreDto);
                if (!createViolations.isEmpty()) {
                    throw ValidationException.create("collection creation", createViolations);
                }
                collectionService.create(collectionStoreDto);
            } else {
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
                collectionService.update(collectionUpdateDto);
            }

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();

            messageManager.showInfoAlert(
                    "Успіх", "Колекцію успішно збережено", collectionViewModel.toString());

        } catch (ValidationException e) {
            String errorMessages =
                    e.getViolations().stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining("; "));
            messageManager.showErrorAlert(
                    "Помилка валідації", "Неправильні введені дані", errorMessages);
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка", "Не вийшло зберегти колекцію", "Помилка: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
