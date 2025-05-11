package com.renata;

import com.renata.domain.entities.Item;
import com.renata.domain.entities.Transaction;
import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.contract.TransactionRepository;
import java.time.format.DateTimeFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

/**
 * Основний клас додатку для демонстрації вибірки авторів із бази даних.
 */
public class Application {

    private final PersistenceContext persistenceContext;
    private final ItemRepository itemRepository;
    private final TransactionRepository transactionRepository;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;

    public Application(PersistenceContext persistenceContext,
        ItemRepository itemRepository,
        TransactionRepository transactionRepository,
        PersistenceInitializer persistenceInitializer,
        ConnectionPool connectionPool) {
        this.persistenceContext = persistenceContext;
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
    }

    /**
     * Виконує ініціалізацію бази даних і виводить всі предмети та транзакції у консоль..
     */
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        persistenceInitializer.clearData();
        persistenceInitializer.init();

        System.out.println("\nAll items in database:");
        itemRepository.findAll().forEach(item ->
            System.out.println(item.getId() + " - " + item.getName())
        );

        List<Item> items = itemRepository.findAll();
        System.out.println("\nAntique Items in Collection:");
        items.forEach(item ->
            System.out.printf("ID: %s\nName: %s\nType: %s\nYear: %d\nCondition: %s\n\n",
                item.getId(), item.getName(), item.getType(), item.getProductionYear(), item.getCondition())
        );

        System.out.println("\nAll transactions in database:");
        transactionRepository.findAll().forEach(tx ->
            System.out.println(tx.getId() + " - " + tx.getType() + " - " + tx.getItemId())
        );

        List<Transaction> transactions = transactionRepository.findByUserId(UUID.fromString("e0284737-9f11-4f80-a732-8fae9fcd7ebd"));
        System.out.println("\nUser Transactions:");
        transactions.forEach(tx ->
            System.out.printf("ID: %s\nType: %s\nItem: %s\nDate: %s\n\n",
                tx.getId(), tx.getType(), tx.getItemId(), tx.getTimestamp().format(formatter))
        );

        connectionPool.shutdown();
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(
            InfrastructureConfig.class,
            AppConfig.class
        );
        Application app = context.getBean(Application.class);
        app.run();
    }

    @Configuration
    static class AppConfig {
        @Bean
        public Application application(PersistenceContext persistenceContext,
            ItemRepository itemRepository,
            TransactionRepository transactionRepository,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool) {
            return new Application(
                persistenceContext,
                itemRepository,
                transactionRepository,
                persistenceInitializer,
                connectionPool
            );
        }
    }
}