package com.renata.presentation.controller.market;

import atlantafx.base.theme.Styles;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.dto.MarketInfoStoreDto;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для відображення ринкової інформації з графіком цін та списком предметів. */
@Component
public class MarketController {
    // TODO: fix purchase system - add it to the marketInfoService and don't let unauthorized users buy items
    @Autowired private MarketInfoService marketInfoService;
    @Autowired private ItemService itemService;

    @FXML private TextField searchField;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshButton;
    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> typeColumn;
    @FXML private TableColumn<Item, Void> actionColumn;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();
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

        if (xAxis == null
                || yAxis == null
                || priceChart == null
                || itemTable == null
                || actionColumn == null) {
            showErrorAlert(
                    "Помилка ініціалізації",
                    "Не вдалося ініціалізувати графік або таблицю: один або більше елементів не"
                            + " завантажено.");
            return;
        }

        // Initialize chart
        priceChart.setTitle("Зміна ціни за часом");

        // Initialize table
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
                                        + e.getMessage());
                    }
                    return new SimpleStringProperty("N/A");
                });

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

        // Initialize action column with Buy button
        actionColumn.setCellFactory(
                column ->
                        new TableCell<Item, Void>() {
                            private final Button buyButton = new Button();

                            {
                                buyButton.getStyleClass().add(Styles.SUCCESS);
                                buyButton.setGraphic(new FontIcon("bx-dollar"));
                                buyButton.setOnAction(
                                        event -> {
                                            Item item = getTableView().getItems().get(getIndex());
                                            handleBuy(item);
                                        });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    Item tableItem = getTableView().getItems().get(getIndex());
                                    try {
                                        Optional<MarketInfo> marketInfoOpt =
                                                marketInfoService.findLatestMarketInfo(
                                                        tableItem.getId());
                                        boolean isPurchased =
                                                marketInfoOpt.isPresent()
                                                        && marketInfoOpt.get().getType()
                                                                == MarketEventType.PURCHASED;
                                        buyButton.setDisable(isPurchased);
                                    } catch (Exception e) {
                                        buyButton.setDisable(true);
                                        System.err.println(
                                                "Error checking purchase status for item "
                                                        + tableItem.getId()
                                                        + ": "
                                                        + e.getMessage());
                                    }
                                    setGraphic(buyButton);
                                }
                            }
                        });

        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemTable.setItems(itemList);

        // Add selection listener to update chart on item click
        itemTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldSelection, newSelection) -> {
                            if (newSelection != null) {
                                updatePriceChart(List.of(newSelection));
                            } else {
                                updatePriceChart(itemList);
                            }
                        });

        // Load initial data
        loadItems();
    }

    @FXML
    private void onRefresh() {
        applySearch();
    }

    @FXML
    private void applySearch() {
        try {
            String searchText = searchField.getText() != null ? searchField.getText().trim() : "";
            List<Item> filteredItems;

            if (!searchText.isEmpty()) {
                filteredItems = itemService.findByName(searchText);
            } else {
                filteredItems = itemService.findAll(0, 100);
            }

            itemList.setAll(filteredItems);
            updatePriceChart(filteredItems);
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка пошуку", "Не вийшло застосувати пошуковий запит: " + e.getMessage());
        }
    }

    @FXML
    private void clearSearch() {
        try {
            searchField.clear();
            loadItems();
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка очистки пошуку",
                    "Не вийшло очистити пошуковий запит: " + e.getMessage());
        }
    }

    private void handleBuy(Item item) {
        try {
            Optional<MarketInfo> latestMarketInfo =
                    marketInfoService.findLatestMarketInfo(item.getId());
            if (latestMarketInfo.isPresent()
                    && latestMarketInfo.get().getType() == MarketEventType.PURCHASED) {
                showErrorAlert("Помилка покупки", "Предмет уже придбано.");
                return;
            }

            double price = latestMarketInfo.isPresent() ? latestMarketInfo.get().getPrice() : 0.0;
            MarketInfoStoreDto purchaseDto =
                    new MarketInfoStoreDto(
                            price, item.getId(), LocalDateTime.now(), MarketEventType.PURCHASED);

            marketInfoService.create(purchaseDto);

            // Refresh table and chart
            itemTable.refresh();
            Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getId().equals(item.getId())) {
                updatePriceChart(List.of(selectedItem));
            } else {
                updatePriceChart(itemList);
            }

            showInfoAlert(
                    "Успішна покупка",
                    "Предмет '"
                            + item.getName()
                            + "' успішно придбано за "
                            + String.format("%.2f", price)
                            + " USD.");
        } catch (Exception e) {
            showErrorAlert("Помилка покупки", "Не вдалося придбати предмет: " + e.getMessage());
        }
    }

    private void loadItems() {
        try {
            List<Item> items = itemService.findAll(0, 100);
            itemList.clear();
            itemList.addAll(items);
            updatePriceChart(items);
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка завантаження предметів",
                    "Не вийшло завантажити предмети: " + e.getMessage());
        }
    }

    private void updatePriceChart(List<Item> items) {
        try {
            priceChart.getData().clear();

            for (Item item : items) {
                List<MarketInfo> marketInfos = marketInfoService.findByItemId(item.getId());
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Ціна предмета " + item.getId());

                marketInfos.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));
                for (MarketInfo info : marketInfos) {
                    String timestamp = info.getTimestamp().format(DATE_TIME_FORMATTER);
                    Double price = info.getPrice();
                    XYChart.Data<String, Number> data = new XYChart.Data<>(timestamp, price);

                    // Create and attach tooltip with immediate show
                    Tooltip tooltip =
                            new Tooltip(String.format("Ціна: %.2f\nЧас: %s", price, timestamp));
                    tooltip.setShowDelay(Duration.ZERO);
                    Tooltip.install(data.getNode(), tooltip);

                    series.getData().add(data);
                }

                // Ensure tooltips are applied after nodes are rendered
                Platform.runLater(
                        () -> {
                            for (XYChart.Data<String, Number> data : series.getData()) {
                                if (data.getNode() != null) {
                                    Tooltip tooltip =
                                            new Tooltip(
                                                    String.format(
                                                            "Ціна: %.2f\nЧас: %s",
                                                            data.getYValue().doubleValue(),
                                                            data.getXValue()));
                                    tooltip.setShowDelay(Duration.ZERO);
                                    Tooltip.install(data.getNode(), tooltip);
                                }
                            }
                        });

                if (!series.getData().isEmpty()) {
                    priceChart.getData().add(series);
                }
            }
        } catch (Exception e) {
            showErrorAlert(
                    "Помилка оновлення графіку", "Не вийшло оновити графік цін: " + e.getMessage());
        }
    }

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
}
