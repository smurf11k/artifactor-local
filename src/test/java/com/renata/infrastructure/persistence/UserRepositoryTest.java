package com.renata.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.renata.domain.entities.Collection;
import com.renata.domain.entities.User;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.contract.UserRepository;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {InfrastructureConfig.class})
@TestInstance(Lifecycle.PER_CLASS)
class UserRepositoryTest {

    private final UserRepository userRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;
    private final PersistenceContext persistenceContext;

    @Autowired
    public UserRepositoryTest(
            UserRepository userRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool,
            PersistenceContext persistenceContext) {
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
    void closeResources() {
        connectionPool.shutdown();
    }

    @Test
    void shouldSaveAndRetrieveUserByUsernameWhenPersisted() {
        String username = "testuser_" + UUID.randomUUID().toString().substring(0, 4);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);

        persistenceContext.registerNew(user);
        persistenceContext.commit();

        List<User> foundUsers = userRepository.findByUsername(username);
        assertThat(foundUsers)
                .hasSize(1)
                .first()
                .extracting(User::getUsername, User::getEmail)
                .containsExactly(username, username + "@example.com");
    }

    @Test
    void shouldSaveAndRetrieveUserByEmailWhenPersisted() {
        String uniqueEmail =
                "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail(uniqueEmail);
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);

        persistenceContext.registerNew(user);
        persistenceContext.commit();

        List<User> users = userRepository.findByEmail(uniqueEmail);
        assertThat(users).hasSize(1);
    }

    @Test
    void shouldFindCollectionsByUserIdWhenCollectionsExist() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("robert");
        user.setEmail("bob@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        Collection collection1 =
                new Collection(UUID.randomUUID(), userId, "Favorites", LocalDateTime.now());
        Collection collection2 =
                new Collection(
                        UUID.randomUUID(),
                        userId,
                        "Ancient Greek",
                        LocalDateTime.now().plusSeconds(1));

        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        List<Collection> collections = userRepository.findCollectionsByUserId(userId);
        assertThat(collections)
                .hasSize(2)
                .extracting(Collection::getName)
                .containsExactly("Favorites", "Ancient Greek");
    }

    @Test
    void shouldReturnEmptyListWhenNoCollectionsForUserId() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("nobody");
        user.setEmail("nobody@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        List<Collection> collections = userRepository.findCollectionsByUserId(userId);

        assertThat(collections).isEmpty();
    }

    @Test
    void shouldFindUsersByPartialUsernameWhenMatchesExist() {
        String prefix = "test_john_" + UUID.randomUUID().toString().substring(0, 4);

        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername(prefix + "smith");
        user1.setEmail("john.smith@example.com");
        user1.setPasswordHash("hashedpassword");
        user1.setRole(Role.GENERAL);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername(prefix + "wick");
        user2.setEmail("john.wick@example.com");
        user2.setPasswordHash("hashedpassword");
        user2.setRole(Role.GENERAL);

        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.commit();

        List<User> users = userRepository.findByPartialUsername(prefix);
        assertThat(users)
                .hasSize(2)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder(prefix + "smith", prefix + "wick");
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersMatchPartialUsername() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("johnsmith");
        user.setEmail("john.smith@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        List<User> users = userRepository.findByPartialUsername("nonexistent");

        assertThat(users).isEmpty();
    }

    @Test
    void shouldCountCollectionsByUserIdWhenCollectionsExist() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("mary");
        user.setEmail("mary@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        Collection collection1 =
                new Collection(UUID.randomUUID(), userId, "Asian Culture", LocalDateTime.now());
        Collection collection2 =
                new Collection(UUID.randomUUID(), userId, "From Rome", LocalDateTime.now());

        persistenceContext.registerNew(collection1);
        persistenceContext.registerNew(collection2);
        persistenceContext.commit();

        long count = userRepository.countCollectionsByUserId(userId);
        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroWhenNoCollectionsForUserId() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("nocollections");
        user.setEmail("nocollections@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);
        persistenceContext.registerNew(user);
        persistenceContext.commit();

        long count = userRepository.countCollectionsByUserId(userId);

        assertThat(count).isEqualTo(0);
    }

    @Test
    void shouldReturnTrueWhenUserExistsByUsername() {
        String username = "exists_test_" + UUID.randomUUID().toString().substring(0, 4);

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);

        persistenceContext.registerNew(user);
        persistenceContext.commit();

        boolean exists = userRepository.existsByUsername(username);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByUsername() {
        boolean exists = userRepository.existsByUsername("nonexistent");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenUserExistsByEmail() {
        String uniqueEmail = "test_unique_" + UUID.randomUUID() + "@example.com";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail(uniqueEmail);
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);

        persistenceContext.registerNew(user);
        persistenceContext.commit();

        boolean exists = userRepository.existsByEmail(uniqueEmail);
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExistByEmail() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldDeleteUserAndVerifyAbsence() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername("deleteuser");
        user.setEmail("deleteuser@example.com");
        user.setPasswordHash("hashedpassword");
        user.setRole(Role.GENERAL);

        persistenceContext.registerNew(user);
        persistenceContext.commit();

        assertThat(userRepository.findById(userId)).isPresent();

        persistenceContext.registerDeleted(user);
        persistenceContext.commit();

        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void shouldSaveMultipleUsersAndRetrieveAll() {
        String prefix = "test_multi_" + UUID.randomUUID().toString().substring(0, 8);

        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setUsername(prefix + "_1");
        user1.setEmail(prefix + "_1@example.com");
        user1.setPasswordHash("hashedpassword");
        user1.setRole(Role.GENERAL);

        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setUsername(prefix + "_2");
        user2.setEmail(prefix + "_2@example.com");
        user2.setPasswordHash("hashedpassword");
        user2.setRole(Role.GENERAL);

        persistenceContext.registerNew(user1);
        persistenceContext.registerNew(user2);
        persistenceContext.commit();

        List<User> allUsers = userRepository.findAll();
        assertThat(allUsers)
                .extracting(User::getUsername)
                .contains(user1.getUsername(), user2.getUsername());
    }
}
