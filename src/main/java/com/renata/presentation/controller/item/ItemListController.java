package com.renata.presentation.controller.item;

import com.renata.application.contract.ItemService;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Контролер для табличного відображення списку предметів антикваріату. */
@Component
public class ItemListController {

    @Autowired private ItemService itemService;

    @FXML private TableView<Item> itemTable;

    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, AntiqueType> typeColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, String> productionYearColumn;
    @FXML private TableColumn<Item, String> countryColumn;
    @FXML private TableColumn<Item, ItemCondition> conditionColumn;
    @FXML private TableColumn<Item, ImageView> imageColumn;

    private ObservableList<Item> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind columns to Item properties
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        productionYearColumn.setCellValueFactory(new PropertyValueFactory<>("productionYear"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        conditionColumn.setCellValueFactory(new PropertyValueFactory<>("condition"));

        // Custom cell factory for image column
        imageColumn.setCellValueFactory(
                cellData -> {
                    String imagePath = cellData.getValue().getImagePath();
                    ImageView imageView = new ImageView();
                    if (imagePath != null) {
                        try {
                            Image image = new Image("file:" + imagePath, 50, 50, true, true);
                            imageView.setImage(image);
                        } catch (Exception e) {
                            imageView.setImage(
                                    new Image("/images/placeholder.png")); // Fallback image
                        }
                    }
                    return new SimpleObjectProperty<>(imageView);
                });

        itemTable.setItems(itemList);
        loadItems();
    }

    @FXML
    private void onRefresh() {
        loadItems();
    }

    @FXML
    private void onBack() {
        // Logic to navigate back (e.g., to ItemController view)
        System.out.println("Navigating back...");
    }

    private void loadItems() {
        try {
            // Fetch all items (adjust offset and limit as needed)
            List<Item> items = itemService.findAll(0, 100); // Example: fetch first 100 items
            itemList.clear();
            itemList.addAll(items);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Load Items");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
