package com.renata.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemRepositoryTest {

    @Mock private ItemRepository itemRepository;

    private Faker faker;
    private Item item;
    private UUID itemId;
    private String name;
    private String country;
    private UUID collectionId;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        itemId = UUID.randomUUID();
        name = faker.commerce().productName();
        country = faker.country().name();
        collectionId = UUID.randomUUID();

        item = new Item();
        item.setId(itemId);
        item.setName(name);
        item.setType(AntiqueType.ANTIQUE);
        item.setDescription(faker.lorem().sentence());
        item.setProductionYear("1890");
        item.setCountry(country);
        item.setCondition(ItemCondition.GOOD);
        item.setImagePath("/images/image.png");
    }

    @Test
    void findByName_ReturnsItemList() {
        when(itemRepository.findByName(name)).thenReturn(List.of(item));

        List<Item> result = itemRepository.findByName(name);

        assertEquals(1, result.size());
        assertEquals(name, result.get(0).getName());
        verify(itemRepository).findByName(name);
    }

    @Test
    void findByType_ReturnsItemList() {
        AntiqueType type = AntiqueType.ANTIQUE;
        when(itemRepository.findByType(type)).thenReturn(List.of(item));

        List<Item> result = itemRepository.findByType(type);

        assertEquals(1, result.size());
        assertEquals(type, result.get(0).getType());
        verify(itemRepository).findByType(type);
    }

    @Test
    void findByCountry_ReturnsItemList() {
        when(itemRepository.findByCountry(country)).thenReturn(List.of(item));

        List<Item> result = itemRepository.findByCountry(country);

        assertEquals(1, result.size());
        assertEquals(country, result.get(0).getCountry());
        verify(itemRepository).findByCountry(country);
    }

    @Test
    void findByCondition_ReturnsItemList() {
        ItemCondition condition = ItemCondition.GOOD;
        when(itemRepository.findByCondition(condition)).thenReturn(List.of(item));

        List<Item> result = itemRepository.findByCondition(condition);

        assertEquals(1, result.size());
        assertEquals(condition, result.get(0).getCondition());
        verify(itemRepository).findByCondition(condition);
    }

    @Test
    void findItemsByCollectionId_ReturnsItems() {
        when(itemRepository.findItemsByCollectionId(collectionId)).thenReturn(List.of(item));

        List<Item> result = itemRepository.findItemsByCollectionId(collectionId);

        assertEquals(1, result.size());
        assertEquals(collectionId, collectionId);
        verify(itemRepository).findItemsByCollectionId(collectionId);
    }
}
