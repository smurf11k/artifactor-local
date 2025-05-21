package com.renata.infrastructure.persistence;

import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemRepositoryTest {

    private static final String TEST_ITEM_NAME = "Ancient Vase";
    private static final String TEST_COUNTRY = "China";
    private static final String TEST_DESCRIPTION = "Ming dynasty vase";
    private static final String TEST_IMAGE_PATH = "/images/vase.jpg";
    private static final String TEST_PRODUCTION_YEAR = "1500";
    private static final AntiqueType TEST_TYPE = AntiqueType.ANTIQUE;
    private static final ItemCondition TEST_CONDITION = ItemCondition.EXCELLENT;

    private final ItemRepository itemRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;

    @Autowired
    public ItemRepositoryTest(
        ItemRepository itemRepository,
        PersistenceInitializer persistenceInitializer,
        ConnectionPool connectionPool) {
        this.itemRepository = itemRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
    }

    @BeforeEach
    void setUp() {
        persistenceInitializer.init(false);
        persistenceInitializer.clearData();
    }

    @AfterAll
    void tearDown() {
        connectionPool.shutdown();
    }

    private Item createAndSaveItem() {
        return createAndSaveItem(TEST_ITEM_NAME, TEST_PRODUCTION_YEAR);
    }

    private Item createAndSaveItem(String name, String productionYear) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName(name);
        item.setType(TEST_TYPE);
        item.setDescription(TEST_DESCRIPTION);
        item.setProductionYear(productionYear);
        item.setCountry(TEST_COUNTRY);
        item.setCondition(TEST_CONDITION);
        item.setImagePath(TEST_IMAGE_PATH);
        return itemRepository.save(item);
    }

    @Test
    void shouldSaveAndRetrieveItemById() {
        Item item = createAndSaveItem();

        Item found = itemRepository.findById(item.getId())
            .orElseThrow(() -> new AssertionError("Item not found"));
        assertThat(found.getName()).isEqualTo(TEST_ITEM_NAME);
        assertThat(found.getType()).isEqualTo(TEST_TYPE);
        assertThat(found.getCountry()).isEqualTo(TEST_COUNTRY);
        assertThat(found.getCondition()).isEqualTo(TEST_CONDITION);
        assertThat(found.getProductionYear()).isEqualTo(TEST_PRODUCTION_YEAR);
    }

    @Test
    void shouldFindItemsByName() {
        Item item = createAndSaveItem();

        List<Item> items = itemRepository.findByName(TEST_ITEM_NAME);
        assertThat(items).hasSize(1)
            .first()
            .satisfies(i -> {
                assertThat(i.getName()).isEqualTo(TEST_ITEM_NAME);
                assertThat(i.getType()).isEqualTo(TEST_TYPE);
            });
    }

    @Test
    void shouldFindItemsByType() {
        Item item = createAndSaveItem();

        List<Item> items = itemRepository.findByType(TEST_TYPE);
        assertThat(items).hasSize(1)
            .first()
            .satisfies(i -> {
                assertThat(i.getName()).isEqualTo(TEST_ITEM_NAME);
                assertThat(i.getType()).isEqualTo(TEST_TYPE);
            });
    }

    @Test
    void shouldFindItemsByCountry() {
        Item item = createAndSaveItem();

        List<Item> items = itemRepository.findByCountry(TEST_COUNTRY);
        assertThat(items).hasSize(1)
            .first()
            .satisfies(i -> {
                assertThat(i.getName()).isEqualTo(TEST_ITEM_NAME);
                assertThat(i.getCountry()).isEqualTo(TEST_COUNTRY);
            });
    }

    @Test
    void shouldFindItemsByCondition() {
        Item item = createAndSaveItem();

        List<Item> items = itemRepository.findByCondition(TEST_CONDITION);
        assertThat(items).hasSize(1)
            .first()
            .satisfies(i -> {
                assertThat(i.getName()).isEqualTo(TEST_ITEM_NAME);
                assertThat(i.getCondition()).isEqualTo(TEST_CONDITION);
            });
    }

    @Test
    void shouldUpdateItemAndVerifyChanges() {
        Item item = createAndSaveItem();
        String updatedName = "Updated Vase";
        item.setName(updatedName);
        itemRepository.save(item);

        Item updated = itemRepository.findById(item.getId())
            .orElseThrow(() -> new AssertionError("Updated item not found"));
        assertThat(updated.getName()).isEqualTo(updatedName);
        assertThat(updated.getType()).isEqualTo(TEST_TYPE);
        assertThat(updated.getProductionYear()).isEqualTo(TEST_PRODUCTION_YEAR);
    }

    @Test
    void shouldDeleteItemAndVerifyAbsence() {
        Item item = createAndSaveItem();
        itemRepository.delete(item.getId());

        assertThat(itemRepository.findById(item.getId())).isEmpty();
    }

    @Test
    void shouldHandleMultipleItemsWithSameName() {
        Item item1 = createAndSaveItem(TEST_ITEM_NAME + "_1", TEST_PRODUCTION_YEAR);
        Item item2 = createAndSaveItem(TEST_ITEM_NAME + "_1", "1600");

        List<Item> items = itemRepository.findByName(TEST_ITEM_NAME + "_1");
        assertThat(items).hasSize(2)
            .extracting(Item::getProductionYear)
            .containsExactlyInAnyOrder(TEST_PRODUCTION_YEAR, "1600");
    }
}