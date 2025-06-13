package com.renata.infrastructure.persistence.util;

import com.renata.domain.util.ItemTestDataGenerator;
import com.renata.domain.util.MarketInfoPriceGenerator;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Ініціалізатор бази даних для виконання DDL та DML скриптів. */
@Component
public class PersistenceInitializer {
    private static final String DDL_SCRIPT_PATH = "db/ddl_h2.sql";
    private static final String DML_SCRIPT_PATH = "db/dml_h2.sql";
    private static final String CLEAR_SCRIPT_PATH = "db/ddl_clear_data_h2.sql";
    private final ConnectionPool connectionPool;
    private final ItemTestDataGenerator itemTestDataGenerator;
    private final MarketInfoPriceGenerator marketInfoPriceGenerator;

    /**
     * Конструктор ініціалізатора.
     *
     * @param connectionPool пул з'єднань для управління з'єднаннями
     * @param itemTestDataGenerator генератор тестових даних для предметів
     * @param marketInfoPriceGenerator генератор ринкових цін
     */
    public PersistenceInitializer(
            ConnectionPool connectionPool,
            ItemTestDataGenerator itemTestDataGenerator,
            MarketInfoPriceGenerator marketInfoPriceGenerator) {
        this.connectionPool = connectionPool;
        this.itemTestDataGenerator = itemTestDataGenerator;
        this.marketInfoPriceGenerator = marketInfoPriceGenerator;
    }

    /** Ініціалізація бази даних: виконання DDL, DML скриптів та генерація тестових даних. */
    public void init() {
        init(true);
    }

    /** Ініціалізація бази даних: виконання DDL, DML скриптів та генерація тестових даних. */
    public void init(boolean isRunDml) {
        try (Connection connection = connectionPool.getConnection();
                Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);

            // Execute DDL script
            statement.execute(getSQL(DDL_SCRIPT_PATH));
            // Execute DML script if specified
            if (isRunDml) {
                statement.execute(getSQL(DML_SCRIPT_PATH));
            }
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка ініціалізації бази даних", e);
        }

        // Generate test data and initial market info if DML is run
        if (isRunDml) {
            itemTestDataGenerator.generateTestData();
            marketInfoPriceGenerator.generateMarketInfo();
        }
    }

    /** Очищення всіх даних у базі без видалення структури. */
    public void clearData() {
        try (Connection connection = connectionPool.getConnection();
                Statement statement = connection.createStatement()) {
            connection.setAutoCommit(false);

            statement.execute(getSQL(CLEAR_SCRIPT_PATH));
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseAccessException("Помилка очищення даних у базі", e);
        }
    }

    /**
     * Зчитування SQL-скрипту з ресурсів.
     *
     * @param resourcePath шлях до SQL-файлу в ресурсах
     * @return вміст SQL-скрипту
     */
    private String getSQL(String resourcePath) {
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                Objects.requireNonNull(
                                        PersistenceInitializer.class
                                                .getClassLoader()
                                                .getResourceAsStream(resourcePath))))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new DatabaseAccessException("Помилка зчитування SQL-скрипту: " + resourcePath, e);
        }
    }
}
