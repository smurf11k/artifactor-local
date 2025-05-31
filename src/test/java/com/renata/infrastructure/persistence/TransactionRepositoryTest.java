package com.renata.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.renata.domain.entities.Item;
import com.renata.domain.entities.Transaction;
import com.renata.domain.entities.User;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.domain.enums.TransactionType;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionRepositoryTest {

    private static final String TEST_ITEM_NAME = "Ancient Vase";
    private static final String TEST_COUNTRY = "China";
    private static final String TEST_DESCRIPTION = "Ming dynasty vase";
    private static final String TEST_IMAGE_PATH = "/images/vase.jpg";
    private static final String TEST_PRODUCTION_YEAR = "1500";
    private static final AntiqueType TEST_TYPE = AntiqueType.ANTIQUE;
    private static final ItemCondition TEST_CONDITION = ItemCondition.EXCELLENT;

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;

    @Autowired
    public TransactionRepositoryTest(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            ItemRepository itemRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
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

    private User createAndSaveUser(String username) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(User.Role.GENERAL);
        return userRepository.save(user);
    }

    private Item createAndSaveItem() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName(TEST_ITEM_NAME);
        item.setType(TEST_TYPE);
        item.setDescription(TEST_DESCRIPTION);
        item.setProductionYear(TEST_PRODUCTION_YEAR);
        item.setCountry(TEST_COUNTRY);
        item.setCondition(TEST_CONDITION);
        item.setImagePath(TEST_IMAGE_PATH);
        return itemRepository.save(item);
    }

    private Transaction createAndSaveTransaction(
            UUID userId, UUID itemId, TransactionType type, LocalDateTime timestamp) {
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setUserId(userId);
        tx.setItemId(itemId);
        tx.setType(type);
        tx.setTimestamp(timestamp);
        return transactionRepository.save(tx);
    }

    @Test
    void shouldFindByUserId() {
        User user = createAndSaveUser("trans_user1");
        Item item = createAndSaveItem();
        Transaction tx =
                createAndSaveTransaction(
                        user.getId(), item.getId(), TransactionType.PURCHASE, LocalDateTime.now());

        List<Transaction> results = transactionRepository.findByUserId(user.getId());
        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(
                        t -> {
                            assertThat(t.getUserId()).isEqualTo(user.getId());
                            assertThat(t.getItemId()).isEqualTo(item.getId());
                            assertThat(t.getType()).isEqualTo(TransactionType.PURCHASE);
                        });
    }

    @Test
    void shouldFindByItemId() {
        User user = createAndSaveUser("trans_user2");
        Item item = createAndSaveItem();
        Transaction tx =
                createAndSaveTransaction(
                        user.getId(), item.getId(), TransactionType.SALE, LocalDateTime.now());

        List<Transaction> results = transactionRepository.findByItemId(item.getId());
        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(
                        t -> {
                            assertThat(t.getItemId()).isEqualTo(item.getId());
                            assertThat(t.getType()).isEqualTo(TransactionType.SALE);
                        });
    }

    @Test
    void shouldFindByType() {
        User user = createAndSaveUser("trans_user3");
        Item item = createAndSaveItem();
        Transaction tx =
                createAndSaveTransaction(
                        user.getId(), item.getId(), TransactionType.PURCHASE, LocalDateTime.now());

        List<Transaction> results = transactionRepository.findByType(TransactionType.PURCHASE);
        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(
                        t -> {
                            assertThat(t.getType()).isEqualTo(TransactionType.PURCHASE);
                            assertThat(t.getItemId()).isEqualTo(item.getId());
                        });
    }

    @Test
    void shouldFindByDateRange() {
        User user = createAndSaveUser("trans_user4");
        Item item1 = createAndSaveItem();
        Item item2 = createAndSaveItem();
        LocalDateTime now = LocalDateTime.now();
        Transaction tx1 =
                createAndSaveTransaction(
                        user.getId(), item1.getId(), TransactionType.PURCHASE, now.minusDays(1));
        Transaction tx2 =
                createAndSaveTransaction(user.getId(), item2.getId(), TransactionType.SALE, now);

        List<Transaction> results =
                transactionRepository.findByDateRange(now.minusDays(2), now.plusDays(1));
        assertThat(results)
                .hasSize(2)
                .extracting(Transaction::getType)
                .containsExactlyInAnyOrder(TransactionType.PURCHASE, TransactionType.SALE);
    }

    @Test
    void shouldFindByUserAndType() {
        User user = createAndSaveUser("trans_user5");
        Item item1 = createAndSaveItem();
        Item item2 = createAndSaveItem();
        Transaction tx1 =
                createAndSaveTransaction(
                        user.getId(), item1.getId(), TransactionType.SALE, LocalDateTime.now());
        Transaction tx2 =
                createAndSaveTransaction(
                        user.getId(), item2.getId(), TransactionType.PURCHASE, LocalDateTime.now());

        List<Transaction> results =
                transactionRepository.findByUserAndType(user.getId(), TransactionType.SALE);
        assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(
                        t -> {
                            assertThat(t.getType()).isEqualTo(TransactionType.SALE);
                            assertThat(t.getUserId()).isEqualTo(user.getId());
                            assertThat(t.getItemId()).isEqualTo(item1.getId());
                        });
    }
}
