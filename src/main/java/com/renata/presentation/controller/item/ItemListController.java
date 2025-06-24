package com.renata.presentation.controller.item;

import atlantafx.base.theme.Styles;
import com.renata.application.contract.AuthService;
import com.renata.application.contract.CollectionService;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.contract.TransactionService;
import com.renata.application.dto.TransactionStoreDto;
import com.renata.application.exception.AuthException;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.entities.Transaction;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.domain.enums.TransactionType;
import com.renata.domain.util.MarketInfoPriceGenerator;
import com.renata.presentation.controller.collection.CollectionManagerController;
import com.renata.presentation.util.MessageManager;
import com.renata.presentation.util.SpringFXMLLoader;
import com.renata.presentation.util.StyleManager;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ItemListController {

    @Autowired private ItemService itemService;
    @Autowired private ApplicationContext context;
    @Autowired private MarketInfoService marketInfoService;
    @Autowired private TransactionService transactionService;
    @Autowired private AuthService authService;
    @Autowired private CollectionService collectionService;
    @Autowired private MessageManager messageManager;
    @Autowired private StyleManager styleManager;
    private Timeline refreshTimeline;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<AntiqueType> typeFilter;
    @FXML private TextField countryFilter;
    @FXML private ComboBox<ItemCondition> conditionFilter;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshButton;
    @FXML private Button addNewButton;
    @FXML private ImageView itemImage;
    @FXML private Label nameLabel;
    @FXML private Label countryLabel;
    @FXML private Label productionYearLabel;
    @FXML private Label typeLabel;
    @FXML private Label conditionLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label timestampLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button organizeButton;

    private final ObservableList<Item> itemList = FXCollections.observableArrayList();
    private Item selectedItem;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        Thread.currentThread()
                .setUncaughtExceptionHandler(
                        (thread, throwable) -> {
                            Platform.runLater(
                                    () ->
                                            messageManager.showErrorAlert(
                                                    "Невідома помилка",
                                                    "Щось пішло не так: ",
                                                    throwable.getMessage()));
                        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(
                cellData -> {
                    Item item = cellData.getValue();
                    try {
                        Optional<MarketInfo> marketInfoOpt =
                                marketInfoService.findLatestMarketInfo(item.getId());
                        if (marketInfoOpt.isPresent()) {
                            return new SimpleStringProperty(
                                    marketInfoOpt.get().getType().toString());
                        }
                    } catch (Exception e) {
                        System.err.println(
                                "Не вийшло знайти стан ринкової ціни для предмету "
                                        + item.getId()
                                        + ": "
                                        + e.getMessage()
                                        + " - Stack trace: "
                                        + getStackTrace(e));
                    }
                    return new SimpleStringProperty("N/A");
                });

        typeColumn.setCellFactory(
                column ->
                        new TableCell<>() {
                            private final Text text = new Text();

                            @Override
                            protected void updateItem(String type, boolean empty) {
                                super.updateItem(type, empty);
                                if (empty || type == null) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    text.setText(type);
                                    StyleManager.applyTypeStyle(text, type);
                                    setGraphic(text);
                                }
                            }
                        });

        actionColumn.setCellFactory(
                column ->
                        new TableCell<>() {
                            private final Button sellButton = new Button();

                            {
                                sellButton.getStyleClass().add(Styles.SUCCESS);
                                sellButton.setGraphic(new FontIcon("bx-dollar"));
                                sellButton.setOnAction(
                                        event -> {
                                            Item item = getTableView().getItems().get(getIndex());
                                            handleSell(item);
                                        });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    Item tableItem = getTableView().getItems().get(getIndex());
                                    boolean canSell = canUserSellItem(tableItem);
                                    sellButton.setDisable(!canSell);
                                    setGraphic(sellButton);
                                }
                            }
                        });

        typeFilter.getItems().addAll(AntiqueType.values());
        conditionFilter.getItems().addAll(ItemCondition.values());
        typeFilter.setValue(null);
        conditionFilter.setValue(null);

        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemTable.setItems(itemList);

        itemTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldSelection, newSelection) -> {
                            selectedItem = newSelection;
                            updateItemDetails();
                            boolean isItemSelected = selectedItem != null;
                            User currentUser = authService.getCurrentUser();
                            boolean isAdmin =
                                    currentUser != null
                                            && authService.hasPermission(
                                                    Role.EntityName.ITEM, "update")
                                            && currentUser.getRole() == Role.ADMIN;
                            editButton.setDisable(!isItemSelected || !isAdmin);
                            deleteButton.setDisable(!isItemSelected || !isAdmin);
                            organizeButton.setDisable(!isItemSelected);
                            addNewButton.setDisable(!isAdmin);
                        });

        loadItems();
        startAutoRefresh();
    }

    public void loadItemsByCollection(UUID collectionId) {
        try {
            List<Item> items = itemService.findItemsByCollectionId(collectionId);
            itemList.clear();
            itemList.addAll(items);
            if (!items.isEmpty()) {
                itemTable.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка завантаження",
                    "Не вийшло завантажити предмети для колекції: ",
                    e.getMessage());
        }
    }

    private boolean canUserSellItem(Item item) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return false;
            }
            UUID userId = currentUser.getId();
            List<Transaction> userTransactions = transactionService.findByUserId(userId);
            boolean hasPurchased =
                    userTransactions.stream()
                            .anyMatch(
                                    t ->
                                            t.getItemId().equals(item.getId())
                                                    && t.getType() == TransactionType.PURCHASE);
            boolean hasSold =
                    userTransactions.stream()
                            .anyMatch(
                                    t ->
                                            t.getItemId().equals(item.getId())
                                                    && t.getType() == TransactionType.SALE);
            return hasPurchased && !hasSold;
        } catch (Exception e) {
            return false;
        }
    }

    private void handleSell(Item item) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return;
            }
            UUID userId = currentUser.getId();
            Optional<MarketInfo> marketInfoOpt =
                    marketInfoService.findLatestMarketInfo(item.getId());
            if (marketInfoOpt.isEmpty()) {
                messageManager.showErrorAlert(
                        "Помилка продажу",
                        "Не знайдено ринкової інформації для предмету: ",
                        item.getName());
                return;
            }
            TransactionStoreDto transactionDto =
                    new TransactionStoreDto(
                            userId, item.getId(), TransactionType.SALE, LocalDateTime.now());
            transactionService.create(transactionDto);
            messageManager.showInfoAlert(
                    "Продаж",
                    "Предмет продано",
                    "Предмет '" + item.getName() + "' успішно продано.");
            loadItems();
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка",
                    "Не вийшло продати предмет '" + item.getName() + "': ",
                    e.getMessage());
        }
    }

    private void updateItemDetails() {
        if (selectedItem == null) {
            itemImage.setImage(null);
            nameLabel.setText("Назва: ");
            countryLabel.setText("Країна: ");
            productionYearLabel.setText("Рік виготовлення: ");
            typeLabel.setText("Тип: ");
            conditionLabel.setText("Стан: ");
            descriptionLabel.setText("Опис: ");
            priceLabel.setText("Ціна: ");
            timestampLabel.setText("Останнє оновлення ціни: ");
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            addNewButton.setDisable(true);
        } else {
            String imagePath = selectedItem.getImagePath();
            try {
                Image image;
                if (imagePath != null && !imagePath.isEmpty()) {
                    image = new Image("file:" + imagePath);
                    itemImage.setPreserveRatio(true);
                    itemImage.setSmooth(true);
                    itemImage.setImage(image);
                } else {
                    image = new Image(getClass().getResourceAsStream("/images/fallback.png"));
                    itemImage.setImage(image);
                }

                if (image.isError()) {
                    throw new Exception("Не вийшло завантажити зображення.");
                }
            } catch (Exception e) {
                try {
                    Image fallback =
                            new Image(getClass().getResourceAsStream("/images/fallback.png"));
                    itemImage.setImage(fallback);
                } catch (Exception ex) {
                    messageManager.showErrorAlert(
                            "Помилка зображення",
                            "Не вийшло завантажити резервне зображення: ",
                            ex.getMessage());
                }
            }
            nameLabel.setText(
                    "Назва: " + (selectedItem.getName() != null ? selectedItem.getName() : ""));
            countryLabel.setText(
                    "Країна: "
                            + (selectedItem.getCountry() != null ? selectedItem.getCountry() : ""));
            productionYearLabel.setText(
                    "Рік виготовлення: "
                            + (selectedItem.getProductionYear() != null
                                    ? selectedItem.getProductionYear()
                                    : ""));
            typeLabel.setText(
                    "Тип: " + (selectedItem.getType() != null ? selectedItem.getType() : ""));
            conditionLabel.setText(
                    "Стан: "
                            + (selectedItem.getCondition() != null
                                    ? selectedItem.getCondition()
                                    : ""));
            descriptionLabel.setText(
                    "Опис: "
                            + (selectedItem.getDescription() != null
                                    ? selectedItem.getDescription()
                                    : ""));
            try {
                Optional<MarketInfo> marketInfoOpt =
                        marketInfoService.findLatestMarketInfo(selectedItem.getId());
                if (marketInfoOpt.isPresent()) {
                    MarketInfo marketInfo = marketInfoOpt.get();
                    priceLabel.setText(
                            "Ціна: " + String.format("%.2f", marketInfo.getPrice()) + " USD");
                    timestampLabel.setText(
                            "Останнє оновлення ціни: "
                                    + marketInfo.getTimestamp().format(DATE_FORMATTER));
                } else {
                    priceLabel.setText("Ціна: N/A");
                    timestampLabel.setText("Останнє оновлення ціни: N/A");
                }
            } catch (Exception e) {
                priceLabel.setText("Ціна: N/A");
                timestampLabel.setText("Останнє оновлення ціни: N/A");
                messageManager.showErrorAlert(
                        "Помилка ринкової інформації",
                        "Не вийшло завантажити рикнову інформацію: ",
                        e.getMessage());
            }
            User currentUser = authService.getCurrentUser();
            boolean isAdmin =
                    currentUser != null
                            && authService.hasPermission(Role.EntityName.ITEM, "update")
                            && currentUser.getRole() == Role.ADMIN;
            editButton.setDisable(!isAdmin);
            deleteButton.setDisable(!isAdmin);
            addNewButton.setDisable(!isAdmin);
        }
    }

    private void startAutoRefresh() {
        refreshTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.minutes(
                                        MarketInfoPriceGenerator.SCHEDULE_INTERVAL_MINUTES),
                                event -> Platform.runLater(this::loadItems)));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    public void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    @FXML
    private void onRefresh() {
        loadItems();
    }

    @FXML
    private void onAddNew() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return;
            }
            if (!authService.hasPermission(Role.EntityName.ITEM, "create")
                    || currentUser.getRole() != Role.ADMIN) {
                messageManager.showErrorAlert(
                        "Немає доступу", "Ви не маєте дозволу на створення предмета.", "");
                return;
            }
            authService.validatePermission(Role.EntityName.ITEM, "create");
            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            URL fxmlUrl = getClass().getResource("/com/renata/view/item/AddItem.fxml");
            if (fxmlUrl == null) {
                throw new Exception("AddItem.fxml не знайдено.");
            }
            Parent root = (Parent) loader.load(fxmlUrl);
            ItemController controller = context.getBean(ItemController.class);
            controller.initForNewItem();
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setTitle("Додавання антикваріату");
            stage.setOnHidden(event -> loadItems());
            stage.show();
        } catch (AuthException e) {
            messageManager.showErrorAlert(
                    "Помилка авторизації", "Не вдалося перевірити права доступу: ", e.getMessage());
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Не вийшло відкрити вікно додавання предмету", "Помилка: ", e.getMessage());
        }
    }

    @FXML
    private void applySearchAndFilters() {
        try {
            List<Item> filteredItems = new ArrayList<>();
            String searchText = searchField.getText() != null ? searchField.getText().trim() : "";
            AntiqueType selectedType = typeFilter.getValue();
            String country = countryFilter.getText() != null ? countryFilter.getText().trim() : "";
            ItemCondition selectedCondition = conditionFilter.getValue();
            if (!searchText.isEmpty()) {
                filteredItems.addAll(itemService.findByName(searchText));
            } else {
                filteredItems.addAll(itemService.findAll(0, 100));
            }
            if (selectedType != null) {
                filteredItems.retainAll(itemService.findByType(selectedType));
            }
            if (!country.isEmpty()) {
                filteredItems.retainAll(itemService.findByCountry(country));
            }
            if (selectedCondition != null) {
                filteredItems.retainAll(itemService.findByCondition(selectedCondition));
            }
            itemList.setAll(filteredItems);
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
            countryFilter.clear();
            conditionFilter.setValue(null);
            loadItems();
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка очищення фільтрів", "Не вийшло очистити фільтри: ", e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedItem != null) {
            try {
                User currentUser = authService.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                if (!authService.hasPermission(Role.EntityName.ITEM, "update")
                        || currentUser.getRole() != Role.ADMIN) {
                    messageManager.showErrorAlert(
                            "Помилка",
                            "Немає доступу",
                            "Ви не маєте дозволу на редагування предмета.");
                    return;
                }
                authService.validatePermission(Role.EntityName.ITEM, "update");
                SpringFXMLLoader loader = new SpringFXMLLoader(context);
                URL fxmlUrl = getClass().getResource("/com/renata/view/item/AddItem.fxml");
                if (fxmlUrl == null) {
                    throw new Exception("AddItem.fxml не знайдено.");
                }
                Parent root = (Parent) loader.load(fxmlUrl);
                ItemController controller = context.getBean(ItemController.class);
                controller.setItem(selectedItem);
                Stage stage = new Stage();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
                stage.setScene(new Scene(root));
                root.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
                stage.setTitle("Редагування антикваріату");
                stage.setOnHidden(event -> loadItems());
                stage.show();
            } catch (AuthException e) {
                messageManager.showErrorAlert(
                        "Помилка авторизації",
                        "Не вдалося перевірити права доступу: ",
                        e.getMessage());
            } catch (Exception e) {
                messageManager.showErrorAlert(
                        "Помилка редагування",
                        "Не вийшло відкрити вікно редагування: ",
                        e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedItem != null) {
            try {
                User currentUser = authService.getCurrentUser();
                if (currentUser == null) {
                    return;
                }
                if (!authService.hasPermission(Role.EntityName.ITEM, "delete")
                        || currentUser.getRole() != Role.ADMIN) {
                    messageManager.showErrorAlert(
                            "Помилка",
                            "Немає доступу",
                            "Ви не маєте дозволу на видалення предмета.");
                    return;
                }
                authService.validatePermission(Role.EntityName.ITEM, "delete");
                itemService.delete(selectedItem.getId());
                loadItems();
                messageManager.showInfoAlert(
                        "Успіх",
                        "Предмет видалено",
                        "Предмет '" + selectedItem.getName() + "' успішно видалено.");
            } catch (AuthException e) {
                messageManager.showErrorAlert(
                        "Помилка авторизації",
                        "Не вдалося перевірити права доступу: ",
                        e.getMessage());
            } catch (Exception e) {
                messageManager.showErrorAlert(
                        "Помилка видалення",
                        "Не вийшло видалити предмет '" + selectedItem.getName() + "': ",
                        e.getMessage());
            }
        }
    }

    private void loadItems() {
        try {
            List<Item> items = itemService.findAll(0, 100);
            itemList.clear();
            itemList.addAll(items);
            if (!items.isEmpty() && selectedItem == null) {
                itemTable.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка завантаження", "Не вийшло завантажити предмети: ", e.getMessage());
        }
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    @FXML
    private void handleOrganize() {
        if (selectedItem == null) {
            return;
        }
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return;
            }
            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            URL fxmlUrl =
                    getClass().getResource("/com/renata/view/collection/CollectionManager.fxml");
            if (fxmlUrl == null) {
                throw new Exception("CollectionManager.fxml не знайдено.");
            }
            Parent root = (Parent) loader.load(fxmlUrl);
            CollectionManagerController controller =
                    context.getBean(CollectionManagerController.class);
            controller.setSelectedItem(selectedItem);
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            stage.setTitle("Менеджер колекцій");
            stage.setOnHidden(event -> loadItems());
            stage.show();
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Не вийшло відкрити менеджер колекцій", "Помилка: ", e.getMessage());
        }
    }
}
