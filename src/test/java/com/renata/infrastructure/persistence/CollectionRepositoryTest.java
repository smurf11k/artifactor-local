package com.renata.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.persistence.contract.CollectionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionRepositoryTest {

    @Mock private CollectionRepository collectionRepository;

    private Faker faker;
    private Collection collection;
    private Item item;
    private UUID collectionId;
    private UUID userId;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        collectionId = UUID.randomUUID();
        userId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        collection = new Collection();
        collection.setId(collectionId);
        collection.setUserId(userId);
        collection.setName(faker.commerce().productName());
        collection.setCreatedAt(LocalDateTime.now());

        item = new Item();
        item.setId(itemId);
        item.setName(faker.commerce().productName());
        item.setType(AntiqueType.ANTIQUE);
        item.setDescription(faker.lorem().sentence());
        item.setProductionYear("1888");
        item.setCountry(faker.country().name());
        item.setCondition(ItemCondition.GOOD);
        item.setImagePath("/images/image.png");
    }

    @Test
    void findByUserId_ReturnsCollections() {
        when(collectionRepository.findByUserId(userId)).thenReturn(List.of(collection));

        List<Collection> result = collectionRepository.findByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(collectionRepository).findByUserId(userId);
    }

    @Test
    void findByItemId_ReturnsCollections() {
        when(collectionRepository.findByItemId(itemId)).thenReturn(List.of(collection));

        List<Collection> result = collectionRepository.findByItemId(itemId);

        assertEquals(1, result.size());
        assertEquals(collectionId, result.get(0).getId());
        verify(collectionRepository).findByItemId(itemId);
    }

    @Test
    void attachItemToCollection_Executes() {
        doNothing().when(collectionRepository).attachItemToCollection(collectionId, itemId);

        collectionRepository.attachItemToCollection(collectionId, itemId);

        verify(collectionRepository).attachItemToCollection(collectionId, itemId);
    }

    @Test
    void detachItemFromCollection_Executes() {
        doNothing().when(collectionRepository).detachItemFromCollection(collectionId, itemId);

        collectionRepository.detachItemFromCollection(collectionId, itemId);

        verify(collectionRepository).detachItemFromCollection(collectionId, itemId);
    }

    @Test
    void countItemsByCollectionId_ReturnsCount() {
        long expectedCount = 3L;
        when(collectionRepository.countItemsByCollectionId(collectionId)).thenReturn(expectedCount);

        long actualCount = collectionRepository.countItemsByCollectionId(collectionId);

        assertEquals(expectedCount, actualCount);
        verify(collectionRepository).countItemsByCollectionId(collectionId);
    }

    @Test
    void findByName_ReturnsCollections() {
        String name = collection.getName();
        when(collectionRepository.findByName(name)).thenReturn(List.of(collection));

        List<Collection> result = collectionRepository.findByName(name);

        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getName());
        verify(collectionRepository).findByName(name);
    }

    @Test
    void clearCollection_Executes() {
        doNothing().when(collectionRepository).clearCollection(collectionId);

        collectionRepository.clearCollection(collectionId);

        verify(collectionRepository).clearCollection(collectionId);
    }
}
