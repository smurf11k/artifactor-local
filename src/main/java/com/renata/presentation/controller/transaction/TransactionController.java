package com.renata.presentation.controller.transaction;

import com.renata.application.contract.TransactionService;
import com.renata.application.dto.TransactionUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.presentation.viewmodel.TransactionViewModel;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для форми редагування транзакції. */
@Component
public class TransactionController {

    @Autowired private TransactionService transactionService;
    @Autowired private Validator validator;

    @FXML private Label idLabel;
    @FXML private TextField userIdField;
    @FXML private TextField itemIdField;
    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private TextField timestampField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private TransactionViewModel transactionViewModel;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll(TransactionType.values());
    }

    public void setTransaction(Transaction transaction) {
        transactionViewModel = new TransactionViewModel(transaction);
        bindFieldsToViewModel();
    }

    private void bindFieldsToViewModel() {
        idLabel.textProperty().bind(transactionViewModel.idProperty().asString());
        userIdField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            try {
                                transactionViewModel
                                        .userIdProperty()
                                        .set(
                                                newValue != null && !newValue.trim().isEmpty()
                                                        ? UUID.fromString(newValue.trim())
                                                        : null);
                            } catch (IllegalArgumentException e) {
                                transactionViewModel.userIdProperty().set(null);
                            }
                        });
        userIdField.setText(
                transactionViewModel.userIdProperty().get() != null
                        ? transactionViewModel.userIdProperty().get().toString()
                        : "");
        itemIdField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            try {
                                transactionViewModel
                                        .itemIdProperty()
                                        .set(
                                                newValue != null && !newValue.trim().isEmpty()
                                                        ? UUID.fromString(newValue.trim())
                                                        : null);
                            } catch (IllegalArgumentException e) {
                                transactionViewModel.itemIdProperty().set(null);
                            }
                        });
        itemIdField.setText(
                transactionViewModel.itemIdProperty().get() != null
                        ? transactionViewModel.itemIdProperty().get().toString()
                        : "");
        typeComboBox.valueProperty().bindBidirectional(transactionViewModel.typeProperty());
        timestampField
                .textProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            try {
                                LocalDateTime parsed =
                                        LocalDateTime.parse(newValue.trim(), DATE_TIME_FORMATTER);
                                transactionViewModel.timestampProperty().set(parsed);
                            } catch (DateTimeParseException e) {
                                transactionViewModel.timestampProperty().set(null);
                            }
                        });
        timestampField.setText(
                transactionViewModel.timestampProperty().get() != null
                        ? transactionViewModel.timestampProperty().get().format(DATE_TIME_FORMATTER)
                        : "");
    }

    @FXML
    private void onSave() {
        try {
            if (transactionViewModel.userIdProperty().get() == null) {
                showErrorAlert(
                        "Помилка валідації",
                        "ID користувача не може бути порожнім",
                        "Введіть коректний UUID для користувача.");
                return;
            }
            if (transactionViewModel.itemIdProperty().get() == null) {
                showErrorAlert(
                        "Помилка валідації",
                        "ID предмета не може бути порожнім",
                        "Введіть коректний UUID для предмета.");
                return;
            }
            if (transactionViewModel.timestampProperty().get() == null) {
                showErrorAlert(
                        "Помилка валідації",
                        "Невірний формат часу",
                        "Введіть час у форматі yyyy-MM-dd HH:mm:ss.");
                return;
            }
            if (transactionViewModel.typeProperty().get() == null) {
                showErrorAlert(
                        "Помилка валідації",
                        "Тип транзакції не може бути порожнім",
                        "Виберіть тип транзакції.");
                return;
            }

            TransactionUpdateDto transactionUpdateDto =
                    new TransactionUpdateDto(
                            transactionViewModel.idProperty().get(),
                            transactionViewModel.typeProperty().get(),
                            transactionViewModel.userIdProperty().get(),
                            transactionViewModel.itemIdProperty().get(),
                            transactionViewModel.timestampProperty().get());

            Set<ConstraintViolation<TransactionUpdateDto>> violations =
                    validator.validate(transactionUpdateDto);
            if (!violations.isEmpty()) {
                throw ValidationException.create("transaction update", violations);
            }

            transactionService.update(transactionUpdateDto.id(), transactionUpdateDto);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();

            showInfoAlert("Успіх", "Транзакцію успішно оновлено", transactionViewModel.toString());

        } catch (ValidationException e) {
            showErrorAlert(
                    "Помилка валідації",
                    "Неправильні введені дані",
                    String.join("; ", e.getMessage()));
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка", "Не вийшло зберегти транзакцію", "Помилка: " + e.getMessage());
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
