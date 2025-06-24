package com.renata.presentation.controller.transaction;

import com.renata.application.contract.TransactionService;
import com.renata.application.dto.TransactionUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.TransactionType;
import com.renata.presentation.util.MessageManager;
import com.renata.presentation.viewmodel.TransactionViewModel;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для форми редагування транзакції. */
@Component
public class TransactionController {

    @Autowired private TransactionService transactionService;
    @Autowired private Validator validator;
    @Autowired private MessageManager messageManager;

    @FXML private Label idLabel;
    @FXML private TextField userIdField;
    @FXML private TextField itemIdField;
    @FXML private ComboBox<TransactionType> typeComboBox;
    @FXML private TextField timestampField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML public HBox buttonBox;

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
            TransactionUpdateDto transactionUpdateDto =
                    new TransactionUpdateDto(
                            transactionViewModel.idProperty().get(),
                            transactionViewModel.userIdProperty().get(),
                            transactionViewModel.itemIdProperty().get(),
                            transactionViewModel.typeProperty().get(),
                            transactionViewModel.timestampProperty().get());

            Set<ConstraintViolation<TransactionUpdateDto>> violations =
                    validator.validate(transactionUpdateDto);
            if (!violations.isEmpty()) {
                throw ValidationException.create("transacrion update", violations);
            }

            transactionService.update(transactionUpdateDto);

            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();

            messageManager.showInfoAlert(
                    "Успіх", "Транзакцію успішно оновлено", transactionViewModel.toString());

        } catch (ValidationException e) {
            String errorMessages =
                    e.getViolations().stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining("; "));
            messageManager.showErrorAlert(
                    "Помилка валідації", "Неправильні введені дані", errorMessages);
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка", "Не вийшло зберегти транзакцію", "Помилка: " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
