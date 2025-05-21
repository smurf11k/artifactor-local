package com.renata.infrastructure.persistence;

import com.renata.domain.entities.Collection;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.User;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.contract.CollectionRepository;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CollectionRepositoryTest {

    private static final String TEST_USER_PREFIX = "test_user_";
    private static final String TEST_COLLECTION_NAME = "Test Collection";
    private static final String TEST_ITEM_NAME = "Test Item";

    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public CollectionRepositoryTest(
        CollectionRepository collectionRepository,
        ItemRepository itemRepository,
        UserRepository userRepository,
        PersistenceInitializer persistenceInitializer,
        ConnectionPool connectionPool,
        PersistenceContext persistenceContext) {
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
        this.persistenceContext = persistenceContext;
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

    private User createAndSaveUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(TEST_USER_PREFIX + UUID.randomUUID().toString().substring(0, 8));
        user.setEmail(user.getUsername() + "@example.com");
        user.setPasswordHash("secure");
        user.setRole(User.Role.GENERAL);
        persistenceContext.registerNew(user);
        persistenceContext.commit();
        return user;
    }

    private Collection createAndSaveCollection(User user, String name) {
        Collection collection = new Collection();
        collection.setId(UUID.randomUUID());
        collection.setUserId(user.getId());
        collection.setName(name);
        collection.setCreatedAt(LocalDateTime.now());
        persistenceContext.registerNew(collection);
        persistenceContext.commit();
        return collection;
    }

    private Item createAndSaveItem(String name) {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName(name);
        item.setType(AntiqueType.ANTIQUE);
        item.setDescription("Test description");
        item.setProductionYear("1800");
        item.setCountry("Italy");
        item.setCondition(ItemCondition.GOOD);
        item.setImagePath("/img/test.jpg");
        persistenceContext.registerNew(item);
        persistenceContext.commit();
        return item;
    }

    @Test
    void shouldSaveAndRetrieveCollectionById() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);

        Collection found = collectionRepository.findById(collection.getId())
            .orElseThrow(() -> new AssertionError("Collection not found"));
        assertThat(found.getName()).isEqualTo(TEST_COLLECTION_NAME);
        assertThat(found.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void shouldFindCollectionsByUserId() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);

        List<Collection> found = collectionRepository.findByUserId(user.getId());
        assertThat(found).hasSize(1)
            .first()
            .satisfies(c -> {
                assertThat(c.getName()).isEqualTo(TEST_COLLECTION_NAME);
                assertThat(c.getUserId()).isEqualTo(user.getId());
            });
    }

    @Test
    void shouldAttachAndDetachItemFromCollection() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);
        Item item = createAndSaveItem(TEST_ITEM_NAME);

        collectionRepository.attachItemToCollection(collection.getId(), item.getId());
        List<Item> items = collectionRepository.findItemsByCollectionId(collection.getId());
        assertThat(items).hasSize(1)
            .first()
            .satisfies(i -> assertThat(i.getName()).isEqualTo(TEST_ITEM_NAME));

        collectionRepository.detachItemFromCollection(collection.getId(), item.getId());
        assertThat(collectionRepository.findItemsByCollectionId(collection.getId())).isEmpty();
    }

    @Test
    void shouldFindCollectionsByItemId() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);
        Item item = createAndSaveItem(TEST_ITEM_NAME);

        collectionRepository.attachItemToCollection(collection.getId(), item.getId());
        List<Collection> collections = collectionRepository.findByItemId(item.getId());
        assertThat(collections).hasSize(1)
            .first()
            .satisfies(c -> {
                assertThat(c.getId()).isEqualTo(collection.getId());
                assertThat(c.getName()).isEqualTo(TEST_COLLECTION_NAME);
            });
    }

    @Test
    void shouldCountItemsInCollection() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);
        Item item1 = createAndSaveItem(TEST_ITEM_NAME + "_1");
        Item item2 = createAndSaveItem(TEST_ITEM_NAME + "_2");

        collectionRepository.attachItemToCollection(collection.getId(), item1.getId());
        collectionRepository.attachItemToCollection(collection.getId(), item2.getId());
        long count = collectionRepository.countItemsByCollectionId(collection.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldClearCollection() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);
        Item item = createAndSaveItem(TEST_ITEM_NAME);

        collectionRepository.attachItemToCollection(collection.getId(), item.getId());
        assertThat(collectionRepository.findItemsByCollectionId(collection.getId())).hasSize(1);
        collectionRepository.clearCollection(collection.getId());
        assertThat(collectionRepository.findItemsByCollectionId(collection.getId())).isEmpty();
    }

    @Test
    void shouldFindCollectionsByName() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);

        List<Collection> collections = collectionRepository.findByName(TEST_COLLECTION_NAME);
        assertThat(collections).hasSize(1)
            .first()
            .satisfies(c -> {
                assertThat(c.getName()).isEqualTo(TEST_COLLECTION_NAME);
                assertThat(c.getUserId()).isEqualTo(user.getId());
            });
    }

    @Test
    void shouldThrowDatabaseAccessExceptionWhenAttachingInvalidItem() {
        User user = createAndSaveUser();
        Collection collection = createAndSaveCollection(user, TEST_COLLECTION_NAME);
        UUID invalidItemId = UUID.randomUUID();

        assertThrows(DatabaseAccessException.class, () ->
            collectionRepository.attachItemToCollection(collection.getId(), invalidItemId));
    }
}