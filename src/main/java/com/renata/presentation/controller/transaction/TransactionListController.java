package com.renata.presentation.controller.transaction;

import atlantafx.base.theme.Styles;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.TransactionService;
import com.renata.application.contract.UserService;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.Transaction;
import com.renata.domain.entities.User;
import com.renata.domain.enums.TransactionType;
import com.renata.presentation.util.MessageManager;
import com.renata.presentation.util.SpringFXMLLoader;
import com.renata.presentation.util.StyleManager;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Контролер для табличного відображення списку транзакцій. */
@Component
public class TransactionListController {

    @Autowired private TransactionService transactionService;
    @Autowired private UserService userService;
    @Autowired private ItemService itemService;
    @Autowired private ApplicationContext context;
    @Autowired private MessageManager messageManager;
    @Autowired private StyleManager styleManager;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, UUID> idColumn;
    @FXML private TableColumn<Transaction, String> userIdColumn;
    @FXML private TableColumn<Transaction, String> itemIdColumn;
    @FXML private TableColumn<Transaction, TransactionType> typeColumn;
    @FXML private TableColumn<Transaction, String> timestampColumn;
    @FXML private TableColumn<Transaction, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<TransactionType> typeFilter;
    @FXML private TextField usernameFilter;
    @FXML private DatePicker fromDateFilter;
    @FXML private DatePicker toDateFilter;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshButton;
    @FXML private Button generateReportButton;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        Thread.currentThread()
                .setUncaughtExceptionHandler(
                        (thread, throwable) ->
                                Platform.runLater(
                                        () ->
                                                messageManager.showErrorAlert(
                                                        "Невідома помилка",
                                                        "Щост пішло не так: ",
                                                        throwable.getMessage())));

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(
                cellData -> {
                    UUID userId = cellData.getValue().getUserId();
                    if (userId != null) {
                        try {
                            User user = userService.findById(userId);
                            return new SimpleObjectProperty<>(
                                    user != null ? user.getUsername() : "Unknown");
                        } catch (Exception e) {
                            return new SimpleObjectProperty<>("Unknown");
                        }
                    }
                    return new SimpleObjectProperty<>("Unknown");
                });
        itemIdColumn.setCellValueFactory(
                cellData -> {
                    UUID itemId = cellData.getValue().getItemId();
                    if (itemId != null) {
                        Optional<Item> itemOpt = itemService.findById(itemId);
                        return new SimpleObjectProperty<>(
                                itemOpt.map(Item::getName).orElse("Unknown"));
                    }
                    return new SimpleObjectProperty<>("Unknown");
                });
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        timestampColumn.setCellValueFactory(
                cellData ->
                        new SimpleObjectProperty<>(
                                cellData.getValue()
                                        .getTimestamp()
                                        .format(styleManager.DATE_TIME_FORMATTER)));

        actionsColumn.setCellFactory(
                param ->
                        new TableCell<>() {
                            private final Button editButton = new Button();
                            private final Button deleteButton = new Button();

                            {
                                FontIcon editIcon = new FontIcon("bx-pencil");
                                editIcon.setIconSize(16);
                                editButton.setGraphic(editIcon);
                                editButton.getStyleClass().add(Styles.ACCENT);
                                editButton.getStyleClass().add("edit-button");
                                editButton.setOnAction(
                                        event -> {
                                            Transaction transaction =
                                                    getTableView().getItems().get(getIndex());
                                            handleEdit(transaction);
                                        });

                                FontIcon deleteIcon = new FontIcon("bx-trash");
                                deleteIcon.setIconSize(16);
                                deleteButton.setGraphic(deleteIcon);
                                deleteButton.getStyleClass().add(Styles.DANGER);
                                deleteButton.getStyleClass().add("delete-button");
                                deleteButton.setOnAction(
                                        event -> {
                                            Transaction transaction =
                                                    getTableView().getItems().get(getIndex());
                                            handleDelete(transaction);
                                        });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    HBox hbox = new HBox(10, editButton, deleteButton);
                                    setGraphic(hbox);
                                }
                            }
                        });

        typeFilter.getItems().addAll(TransactionType.values());
        typeFilter.setValue(null);

        fromDateFilter.setConverter(styleManager.getLocalDateStringConverter());
        toDateFilter.setConverter(styleManager.getLocalDateStringConverter());

        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionTable.setItems(transactionList);
        loadTransactions();
    }

    @FXML
    private void onRefresh() {
        loadTransactions();
    }

    @FXML
    private void applySearchAndFilters() {
        try {
            List<Transaction> allTransactions = transactionService.findAll(0, Integer.MAX_VALUE);
            List<Transaction> filteredTransactions = new ArrayList<>(allTransactions);

            String searchText = searchField.getText() != null ? searchField.getText().trim() : "";
            TransactionType selectedType = typeFilter.getValue();
            String userIdText =
                    usernameFilter.getText() != null ? usernameFilter.getText().trim() : "";
            LocalDate fromDate = fromDateFilter.getValue();
            LocalDate toDate = toDateFilter.getValue();

            if (fromDate != null || toDate != null) {
                LocalDateTime from = fromDate != null ? fromDate.atStartOfDay() : LocalDateTime.MIN;
                LocalDateTime to =
                        toDate != null ? toDate.atTime(LocalTime.MAX) : LocalDateTime.MAX;
                filteredTransactions.removeIf(
                        t -> t.getTimestamp().isBefore(from) || t.getTimestamp().isAfter(to));
            }

            if (!searchText.isEmpty()) {
                List<Item> items = itemService.findByName(searchText);
                if (!items.isEmpty()) {
                    List<UUID> itemIds = items.stream().map(Item::getId).toList();
                    filteredTransactions.removeIf(
                            t -> t.getItemId() == null || !itemIds.contains(t.getItemId()));
                } else {
                    filteredTransactions.clear();
                }
            }

            if (selectedType != null) {
                filteredTransactions.removeIf(t -> t.getType() != selectedType);
            }

            if (!userIdText.isEmpty()) {
                User user = userService.findByUsername(userIdText);
                if (user != null) {
                    filteredTransactions.removeIf(
                            t -> t.getUserId() == null || !t.getUserId().equals(user.getId()));
                } else {
                    filteredTransactions.clear();
                }
            }

            transactionList.setAll(filteredTransactions);
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка застосування фільтрів",
                    "Не вийшло застосувати фільтри: ",
                    e.getMessage());
        }
    }

    @FXML
    private void clearFilters() {
        try {
            searchField.clear();
            typeFilter.setValue(null);
            fromDateFilter.setValue(null);
            toDateFilter.setValue(null);
            usernameFilter.clear();
            loadTransactions();
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка очистки фільтрів", "Не вийшло очистити фільтри: ", e.getMessage());
        }
    }

    @FXML
    private void generateReport() {
        try {
            Predicate<Transaction> predicate =
                    transaction -> {
                        String searchText =
                                searchField.getText() != null ? searchField.getText().trim() : "";
                        TransactionType selectedType = typeFilter.getValue();
                        String userIdText =
                                usernameFilter.getText() != null
                                        ? usernameFilter.getText().trim()
                                        : "";
                        boolean matches = true;

                        if (!searchText.isEmpty()) {
                            List<Item> items = itemService.findByName(searchText);
                            matches =
                                    items.stream()
                                            .anyMatch(
                                                    item ->
                                                            item.getId()
                                                                    .equals(
                                                                            transaction
                                                                                    .getItemId()));
                        }

                        if (selectedType != null) {
                            matches = matches && transaction.getType().equals(selectedType);
                        }

                        if (!userIdText.isEmpty()) {
                            User user = userService.findByUsername(userIdText);
                            matches =
                                    matches
                                            && user != null
                                            && user.getId().equals(transaction.getUserId());
                        }

                        return matches;
                    };

            transactionService.generateReport(predicate);
            messageManager.showInfoAlert(
                    "Успіх", "Звіт згенеровано", "Звіт транзакцій успішно згенеровано.");
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка генерації звіту", "Не вдалося згенерувати звіт: ", e.getMessage());
        }
    }

    private void handleEdit(Transaction transaction) {
        try {
            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            URL fxmlUrl =
                    getClass().getResource("/com/renata/view/transaction/EditTransaction.fxml");
            Parent root = (Parent) loader.load(fxmlUrl);
            TransactionController controller = context.getBean(TransactionController.class);
            controller.setTransaction(transaction);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setTitle("Редагування транзакції");
            stage.show();
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Не вийшло відкрити вікно редагування транзакції", "Помилка: ", e.getMessage());
        }
    }

    private void handleDelete(Transaction transaction) {
        try {
            transactionService.delete(transaction.getId());
            loadTransactions();
            messageManager.showInfoAlert(
                    "Успіх",
                    "Транзакцію видалено",
                "Транзакцію з ID '" + transaction.getId() + "' успішно видалено.");
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Не вийшло видалити транзакцію",
                    "Помилка видалення транзакції з ID '"
                            + (transaction != null ? transaction.getId() : "unknown")
                            + "': ",
                    e.getMessage());
        }
    }

    private void loadTransactions() {
        try {
            List<Transaction> transactions = transactionService.findAll(0, 100);
            transactionList.clear();
            transactionList.addAll(transactions);
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Не вийшло завантажити транзакції",
                    "Помилка завантаження транзакцій: ",
                    e.getMessage());
        }
    }
}
