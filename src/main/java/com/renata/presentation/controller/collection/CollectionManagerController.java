package com.renata.presentation.controller.collection;

import com.renata.application.contract.AuthService;
import com.renata.application.contract.CollectionService;
import com.renata.application.contract.ItemService;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import com.renata.presentation.util.MessageManager;
import java.util.List;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CollectionManagerController {

    @Autowired private CollectionService collectionService;
    @Autowired private ItemService itemService;
    @Autowired private AuthService authService;
    @Autowired private MessageManager messageManager;

    @FXML private ComboBox<Collection> collectionComboBox;
    @FXML private Button attachButton;
    @FXML private Button detachButton;

    private Item selectedItem;
    private ObservableList<Collection> collections = FXCollections.observableArrayList();

    public void setSelectedItem(Item item) {
        this.selectedItem = item;
        loadCollections();
    }

    @FXML
    public void initialize() {
        collectionComboBox.setItems(collections);
        collectionComboBox.setConverter(
                new StringConverter<>() {
                    @Override
                    public String toString(Collection collection) {
                        return collection != null ? collection.getName() : "";
                    }

                    @Override
                    public Collection fromString(String string) {
                        return collections.stream()
                                .filter(c -> c.getName().equals(string))
                                .findFirst()
                                .orElse(null);
                    }
                });
        collectionComboBox
                .getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> updateButtonStates(newSelection));
    }

    private void loadCollections() {
        try {
            UUID userId = authService.getCurrentUser().getId();
            List<Collection> userCollections = collectionService.findByUserId(userId);
            collections.setAll(userCollections);
            if (!collections.isEmpty()) {
                collectionComboBox.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка завантаження", "Не вийшло завантажити колекції: ", e.getMessage());
        }
    }

    private void updateButtonStates(Collection selectedCollection) {
        if (selectedCollection == null || selectedItem == null) {
            attachButton.setDisable(true);
            detachButton.setDisable(true);
            return;
        }
        try {
            boolean isItemInCollection =
                    itemService.findItemsByCollectionId(selectedCollection.getId()).stream()
                            .anyMatch(item -> item.getId().equals(selectedItem.getId()));
            attachButton.setDisable(isItemInCollection);
            detachButton.setDisable(!isItemInCollection);
        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Помилка", "Не вийшло отримати статус предмету: ", e.getMessage());
        }
    }

    @FXML
    private void handleAttachItem() {
        Collection selectedCollection = collectionComboBox.getValue();
        if (selectedCollection != null && selectedItem != null) {
            try {
                collectionService.attachItemToCollection(
                        selectedCollection.getId(), selectedItem.getId());
                messageManager.showInfoAlert(
                        "Успіх", "Додано до колекції", "Предмет додано до колекції.");
                updateButtonStates(selectedCollection);
            } catch (Exception e) {
                messageManager.showErrorAlert(
                        "Помилка додавання",
                        "Не вийшло додати предмет до колекції: ",
                        e.getMessage());
            }
        }
    }

    @FXML
    private void handleDetachItem() {
        Collection selectedCollection = collectionComboBox.getValue();
        if (selectedCollection != null && selectedItem != null) {
            try {
                collectionService.detachItemFromCollection(
                        selectedCollection.getId(), selectedItem.getId());
                messageManager.showInfoAlert(
                        "Успіх", "Видалено з колекції", "Предмет успішно видалено з колекції.");
                updateButtonStates(selectedCollection);
            } catch (Exception e) {
                messageManager.showErrorAlert(
                        "Помилка видалення",
                        "Не вийшло видалити предмет з колекції: ",
                        e.getMessage());
            }
        }
    }
}
