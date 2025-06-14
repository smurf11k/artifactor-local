package com.renata.presentation.controller.item;

import atlantafx.base.theme.Styles;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.presentation.util.SpringFXMLLoader;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ItemListController {

    @Autowired private ItemService itemService;
    @Autowired private ApplicationContext context;
    @Autowired private MarketInfoService marketInfoService;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> typeColumn;
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

    private ObservableList<Item> itemList = FXCollections.observableArrayList();
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
                                            showErrorAlert(
                                                    "Unexpected Error",
                                                    "An unexpected error occurred: "
                                                            + throwable.getMessage()));
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
                                "Error fetching event type for item "
                                        + item.getId()
                                        + ": "
                                        + e.getMessage()
                                        + " - Stack trace: "
                                        + getStackTrace(e));
                    }
                    return new SimpleStringProperty("N/A");
                });

        // Update the cell factory implementation
        typeColumn.setCellFactory(
                column ->
                        new TableCell<Item, String>() {
                            private final Text text = new Text();

                            @Override
                            protected void updateItem(String type, boolean empty) {
                                super.updateItem(type, empty);

                                if (empty || type == null) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    text.setText(type);
                                    applyTypeStyle(text, type);
                                    setGraphic(text);
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
                        });

        loadItems();
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
        } else {
            String imagePath = selectedItem.getImagePath();
            try {
                Image image =
                        imagePath != null && !imagePath.isEmpty()
                                ? new Image("file:" + imagePath, 150, 150, true, true)
                                : new Image(getClass().getResourceAsStream("/images/fallback.png"));
                if (image.isError()) {
                    throw new Exception("Failed to load image");
                }
                itemImage.setImage(image);
            } catch (Exception e) {
                try {
                    Image fallback =
                            new Image(getClass().getResourceAsStream("/images/fallback.png"));
                    itemImage.setImage(fallback);
                } catch (Exception ex) {
                    showErrorAlert(
                            "Image Error", "Failed to load fallback image: " + ex.getMessage());
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
                showErrorAlert(
                        "Market Info Error", "Failed to load market info: " + e.getMessage());
            }

            editButton.setDisable(false);
            deleteButton.setDisable(false);
        }
    }

    // helper method for text colors
    private void applyTypeStyle(Text text, String type) {
        text.getStyleClass().clear();

        text.getStyleClass().add(Styles.TEXT);

        if (type != null) {
            switch (type) {
                case "LISTED":
                    text.getStyleClass().add(Styles.ACCENT);
                    break;
                case "RELISTED":
                    text.getStyleClass().add(Styles.WARNING);
                    break;
                case "PRICE_UPDATED":
                    text.getStyleClass().add(Styles.SUCCESS);
                    break;
                case "PURCHASED":
                    text.getStyleClass().add(Styles.DANGER);
                    break;
                default:
                    text.getStyleClass().add(Styles.TEXT_SUBTLE);
            }
        }
    }

    @FXML
    private void onRefresh() {
        clearFilters();
        loadItems();
    }

    @FXML
    private void onAddNew() {
        try {
            SpringFXMLLoader loader = new SpringFXMLLoader(context);
            URL fxmlUrl = getClass().getResource("/com/renata/view/item/AddItem.fxml");
            Parent root = (Parent) loader.load(fxmlUrl);
            ItemController controller = context.getBean(ItemController.class);
            controller.initForNewItem();
            Stage stage = new Stage();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            stage.setScene(new Scene(root));
            stage.setTitle("Додавання антикваріату");
            stage.show();
        } catch (Exception e) {
            showErrorAlert("Failed to open add item window", "Error: " + e.getMessage());
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
            showErrorAlert("Filter Error", "Failed to apply filters: " + e.getMessage());
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
            showErrorAlert("Clear Filter Error", "Failed to clear filters: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedItem != null) {
            try {
                SpringFXMLLoader loader = new SpringFXMLLoader(context);
                URL fxmlUrl = getClass().getResource("/com/renata/view/item/AddItem.fxml");
                Parent root = (Parent) loader.load(fxmlUrl);
                ItemController controller = context.getBean(ItemController.class);
                controller.setItem(selectedItem);
                Stage stage = new Stage();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
                stage.setScene(new Scene(root));
                stage.setTitle("Редагування антикваріату");
                stage.show();
            } catch (Exception e) {
                showErrorAlert("Edit Error", "Failed to open edit window: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedItem != null) {
            try {
                itemService.delete(selectedItem.getId());
                loadItems();
                showInfoAlert(
                        "Item Deleted",
                        "Item '" + selectedItem.getName() + "' successfully deleted.");
            } catch (Exception e) {
                showErrorAlert(
                        "Delete Error",
                        "Failed to delete item '"
                                + selectedItem.getName()
                                + "': "
                                + e.getMessage());
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
            showErrorAlert("Load Error", "Failed to load items: " + e.getMessage());
        }
    }

    private void showInfoAlert(String header, String content) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }

    private void showErrorAlert(String header, String content) {
        Platform.runLater(
                () -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.showAndWait();
                });
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
