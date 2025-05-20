package com.renata;

import com.renata.infrastructure.InfrastructureConfig;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.util.ConnectionPool;
import com.renata.infrastructure.persistence.util.PersistenceInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Основний клас додатку */
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

    public void run() {
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
