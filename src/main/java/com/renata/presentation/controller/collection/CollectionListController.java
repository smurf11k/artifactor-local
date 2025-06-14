package com.renata.presentation.controller.collection;

import atlantafx.base.theme.Styles;
import com.renata.application.contract.AuthService;
import com.renata.application.contract.CollectionService;
import com.renata.application.contract.UserService;
import com.renata.application.exception.AuthException;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.presentation.util.SpringFXMLLoader;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

/** Контролер для табличного відображення списку колекцій. */
@Component
public class CollectionListController {

    @Autowired private CollectionService collectionService;
    @Autowired private UserService userService;
    @Autowired private AuthService authService;
    @Autowired private ApplicationContext context;

    @FXML private TableView<Collection> collectionTable;
    @FXML private TableColumn<Collection, String> nameColumn;
    @FXML private TableColumn<Collection, String> usernameColumn;
    @FXML private TableColumn<Collection, String> createdAtColumn;
    @FXML private TableColumn<Collection, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private TextField usernameFilter;
    @FXML private Button addButton;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshButton;

    private ObservableList<Collection> collectionList = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        Thread.currentThread()
                .setUncaughtExceptionHandler(
                        (thread, throwable) ->
                                Platform.runLater(
                                        () ->
                                                showErrorAlert(
                                                        "Unexpected Error",
                                                        "An unexpected error occurred: "
                                                                + throwable.getMessage())));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(
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
        createdAtColumn.setCellValueFactory(
                cellData ->
                        new SimpleObjectProperty<>(
                                cellData.getValue().getCreatedAt().format(DATE_TIME_FORMATTER)));

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
                                            Collection collection =
                                                    getTableView().getItems().get(getIndex());
                                            handleEdit(collection);
                                        });

                                FontIcon deleteIcon = new FontIcon("bx-trash");
                                deleteIcon.setIconSize(16);
                                deleteButton.setGraphic(deleteIcon);
                                deleteButton.getStyleClass().add(Styles.DANGER);
                                deleteButton.getStyleClass().add("delete-button");
                                deleteButton.setOnAction(
                                        event -> {
                                            Collection collection =
                                                    getTableView().getItems().get(getIndex());
                                            handleDelete(collection);
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

        collectionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        collectionTable.setItems(collectionList);
        loadCollections();
    }

    @FXML
    private void onRefresh() {
        clearFilters();
        loadCollections();
    }

    @FXML
    private void applySearchAndFilters() {
        try {
            List<Collection> filteredCollections = new ArrayList<>();
            String searchText = searchField.getText() != null ? searchField.getText().trim() : "";
            String userText =
                    usernameFilter.getText() != null ? usernameFilter.getText().trim() : "";

            if (!searchText.isEmpty()) {
                filteredCollections.addAll(collectionService.findByName(searchText));
            } else {
                filteredCollections.addAll(collectionService.findAll(0, 100));
            }

            if (!userText.isEmpty()) {
                User user = userService.findByUsername(userText);
                if (user != null) {
                    filteredCollections.retainAll(collectionService.findByUserId(user.getId()));
                } else {
                    filteredCollections.clear();
                }
            }

            collectionList.setAll(filteredCollections);
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка застосування фільтрів",
                    "Не вийшло застосувати фільтри: " + e.getMessage());
        }
    }

    @FXML
    private void clearFilters() {
        try {
            searchField.clear();
            usernameFilter.clear();
            loadCollections();
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка очистки фільтрів", "Не вийшло очистити фільтри: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        try {
            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            URL fxmlUrl = getClass().getResource("/com/renata/view/collection/AddCollection.fxml");
            if (fxmlUrl == null) {
                throw new Exception("AddCollection.fxml not found");
            }
            Parent root = (Parent) loader.load(fxmlUrl);
            CollectionController controller = context.getBean(CollectionController.class);
            controller.setNewCollection();
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Додавання нової колекції");
            stage.setOnHidden(event -> loadCollections()); // Refresh table after add
            stage.show();
        } catch (Exception e) {
            showErrorAlert(
                    "Не вийшло відкрити вікно додавання колекції", "Помилка: " + e.getMessage());
        }
    }

    private void handleEdit(Collection collection) {
        try {
            // Check if the current user is authorized to edit this collection
            UUID currentUserId = authService.getCurrentUser().getId();
            if (!collection.getUserId().equals(currentUserId)) {
                showErrorAlert(
                        "Немає доступу", "Ви не маєте дозволу на редагування цієї колекції.");
                return;
            }

            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            URL fxmlUrl = getClass().getResource("/com/renata/view/collection/AddCollection.fxml");
            if (fxmlUrl == null) {
                throw new Exception("AddCollection.fxml not found");
            }
            Parent root = (Parent) loader.load(fxmlUrl);
            CollectionController controller = context.getBean(CollectionController.class);
            controller.setCollection(collection);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Редагування колекції");
            stage.setOnHidden(event -> loadCollections()); // Refresh table after edit
            stage.show();
        } catch (AuthException e) {
            showErrorAlert(
                    "Помилка авторизації",
                    "Не вдалося перевірити права доступу: " + e.getMessage());
        } catch (Exception e) {
            showErrorAlert(
                    "Не вийшло відкрити вікно редагування колекції", "Помилка: " + e.getMessage());
        }
    }

    private void handleDelete(Collection collection) {
        try {
            collectionService.delete(collection.getId());
            loadCollections();
            showInfoAlert(
                    "Колекцію видалено",
                    "Колекцію з ID '" + collection.getId() + "' успішно видалено.");
        } catch (Exception e) {
            showErrorAlert(
                    "Не вийшло видалити колекцію",
                    "Помилка видалення колекції з ID '"
                            + (collection != null ? collection.getId() : "unknown")
                            + "': "
                            + e.getMessage());
        }
    }

    private void loadCollections() {
        try {
            List<Collection> collections = collectionService.findAll(0, 100);
            collectionList.clear();
            collectionList.addAll(collections);
        } catch (Exception e) {
            showErrorAlert(
                    "Не вийшло завантажити колекції",
                    "Помилка завантаження колекцій: " + e.getMessage());
        }
    }

    private void showInfoAlert(String header, String content) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успіх");
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }

    private void showErrorAlert(String header, String content) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Помилка");
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }
}
