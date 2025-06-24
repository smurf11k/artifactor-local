package com.renata.presentation.controller.market;

import atlantafx.base.theme.Styles;
import com.renata.application.contract.AuthService;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.contract.TransactionService;
import com.renata.application.dto.TransactionStoreDto;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import com.renata.domain.enums.TransactionType;
import com.renata.domain.util.MarketInfoPriceGenerator;
import com.renata.presentation.util.MessageManager;
import com.renata.presentation.util.StyleManager;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
    @Autowired private MarketInfoService marketInfoService;
    @Autowired private ItemService itemService;
    @Autowired private TransactionService transactionService;
    @Autowired private AuthService authService;
    @Autowired private MessageManager messageManager;
    @Autowired private StyleManager styleManager;

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
    private Timeline refreshTimeline;

    @FXML
    public void initialize() {
        Thread.currentThread()
                .setUncaughtExceptionHandler(
                        (thread, throwable) ->
                                Platform.runLater(
                                        () ->
                                                messageManager.showErrorAlert(
                                                        "Невідома помилка",
                                                        "Щось пішло не так: ",
                                                        throwable.getMessage())));

        if (xAxis == null
                || yAxis == null
                || priceChart == null
                || itemTable == null
                || actionColumn == null) {
            messageManager.showErrorAlert(
                    "Помилка",
                    "Помилка ініціалізації",
                    "Не вдалося ініціалізувати графік або таблицю: один або більше елементів не"
                            + " завантажено");
            return;
        }

        priceChart.setTitle("Зміна ціни за часом");

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
                                "Помилка вибірки типу ринкової інформації для предмету "
                                        + item.getId()
                                        + ": "
                                        + e.getMessage());
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
                                                "Помилка перевірки статусу покупки предмету "
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

        loadItems();
        startAutoRefresh();
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
            messageManager.showErrorAlert(
                    "Помилка пошуку", "Не вийшло застосувати пошуковий запит: ", e.getMessage());
        }
    }

    @FXML
    private void clearSearch() {
        try {
            searchField.clear();
            loadItems();
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка очистки пошуку",
                    "Не вийшло очистити пошуковий запит: ",
                    e.getMessage());
        }
    }

    private void handleBuy(Item item) {
        try {
            Optional<MarketInfo> latestMarketInfo =
                    marketInfoService.findLatestMarketInfo(item.getId());
            if (latestMarketInfo.isPresent()
                    && latestMarketInfo.get().getType() == MarketEventType.PURCHASED) {
                messageManager.showErrorAlert("Помилка покупки", "Предмет уже придбано.", "");
                return;
            }

            double price = latestMarketInfo.isPresent() ? latestMarketInfo.get().getPrice() : 0.0;
            TransactionStoreDto transactionDto =
                    new TransactionStoreDto(
                            authService.getCurrentUser().getId(),
                            item.getId(),
                            TransactionType.PURCHASE,
                            LocalDateTime.now());

            transactionService.create(transactionDto);

            itemTable.refresh();
            Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getId().equals(item.getId())) {
                updatePriceChart(List.of(selectedItem));
            } else {
                updatePriceChart(itemList);
            }

            messageManager.showInfoAlert(
                    "Успішна покупка",
                    "Предмет '"
                            + item.getName()
                            + "' успішно придбано за "
                            + String.format("%.2f", price)
                            + " USD.",
                    "");
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка покупки", "Не вдалося придбати предмет: ", e.getMessage());
        }
    }

    private void loadItems() {
        try {
            List<Item> items = itemService.findAll(0, 100);
            itemList.clear();
            itemList.addAll(items);
            updatePriceChart(items);
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка завантаження предметів",
                    "Не вийшло завантажити предмети: ",
                    e.getMessage());
        }
    }

    private void updatePriceChart(List<Item> items) {
        try {
            priceChart.getData().clear();

            for (Item item : items) {
                List<MarketInfo> marketInfos = marketInfoService.findByItemId(item.getId());
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Ціна предмета " + item.getId());

                marketInfos.sort(Comparator.comparing(MarketInfo::getTimestamp));
                for (MarketInfo info : marketInfos) {
                    String timestamp = info.getTimestamp().format(styleManager.DATE_TIME_FORMATTER);
                    Double price = info.getPrice();
                    XYChart.Data<String, Number> data = new XYChart.Data<>(timestamp, price);

                    Tooltip tooltip =
                            new Tooltip(String.format("Ціна: %.2f\nЧас: %s", price, timestamp));
                    tooltip.setShowDelay(Duration.ZERO);
                    Tooltip.install(data.getNode(), tooltip);

                    series.getData().add(data);
                }

                Platform.runLater(
                        () -> {
                            for (XYChart.Data<String, Number> data : series.getData()) {
                                if (data.getNode() != null) {
                                    Tooltip tooltip =
                                            new Tooltip(
                                                    String.format(
                                                            "Ціна: %.2f%nЧас: %s",
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
            messageManager.showErrorAlert(
                    "Помилка оновлення графіку", "Не вийшло оновити графік цін: ", e.getMessage());
        }
    }
}
