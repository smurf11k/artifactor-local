package com.renata;

import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import java.time.format.DateTimeFormatter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Основний клас додатку для демонстрації вибірки авторів із бази даних. */
public class Application {

    private final PersistenceContext persistenceContext;
    private final PersistenceInitializer persistenceInitializer;
    private final ConnectionPool connectionPool;

    public Application(
            PersistenceContext persistenceContext,
            PersistenceInitializer persistenceInitializer,
            ConnectionPool connectionPool) {
        this.persistenceContext = persistenceContext;
        this.persistenceInitializer = persistenceInitializer;
        this.connectionPool = connectionPool;
    }

    /** Виконує ініціалізацію бази даних і виводить всі предмети та транзакції у консоль.. */
    public void run() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        persistenceInitializer.clearData();
        persistenceInitializer.init();

        connectionPool.shutdown();
    }

    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(InfrastructureConfig.class, AppConfig.class);
        Application app = context.getBean(Application.class);
        app.run();
    }

    @Configuration
    static class AppConfig {
        @Bean
        public Application application(
                PersistenceContext persistenceContext,
                PersistenceInitializer persistenceInitializer,
                ConnectionPool connectionPool) {
            return new Application(persistenceContext, persistenceInitializer, connectionPool);
        }
    }
}
