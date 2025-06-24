package com.renata.domain.util;

import com.github.javafaker.Faker;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.contract.TransactionService;
import com.renata.application.dto.MarketInfoStoreDto;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.Transaction;
import com.renata.domain.enums.MarketEventType;
import com.renata.domain.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Утиліта яка створює дані ринкової інформації для елементів антикваріату (кожну хвилину). */
@Component
public class MarketInfoPriceGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketInfoPriceGenerator.class);
    private static final double MIN_INITIAL_PRICE = 10.0;
    private static final double MAX_INITIAL_PRICE = 100000.0;
    private static final int MAX_RETRIES = 3;
    public static final long SCHEDULE_INTERVAL_MINUTES = 1;

    @Autowired private ItemService itemService;
    @Autowired private MarketInfoService marketInfoService;
    @Autowired private TransactionService transactionService;
    private final Faker faker = new Faker();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startGeneratingMarketInfo() {
        scheduler.scheduleAtFixedRate(
                this::generateMarketInfo, 0, SCHEDULE_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    public void stopGeneratingMarketInfo() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void generateMarketInfo() {
        try {
            List<Item> items = itemService.findAll(0, Integer.MAX_VALUE);
            List<UUID> skippedItems = new ArrayList<>();

            for (Item item : items) {
                if (item == null || item.getId() == null) {
                    skippedItems.add(null);
                    continue;
                }
                boolean processed = processItem(item);
                if (!processed) {
                    skippedItems.add(item.getId());
                }
            }
            if (!skippedItems.isEmpty()) {
                LOGGER.warn("Пропущено предмети: {}", skippedItems);
            }
        } catch (Exception e) {
            LOGGER.error("Помилка при створенні ринкової інформації", e);
        }
    }

    private boolean processItem(Item item) {
        try {
            List<Transaction> transactions = transactionService.findByItemId(item.getId());
            double newPrice =
                    faker.number()
                            .randomDouble(2, (int) MIN_INITIAL_PRICE, (int) MAX_INITIAL_PRICE);
            MarketEventType eventType;

            if (!transactions.isEmpty()) {
                Transaction latestTransaction =
                        transactions.stream()
                                .max((t1, t2) -> t1.getTimestamp().compareTo(t2.getTimestamp()))
                                .orElse(null);
                if (latestTransaction != null) {
                    TransactionType lastTransactionType = latestTransaction.getType();
                    if (lastTransactionType == TransactionType.PURCHASE) {
                        eventType = MarketEventType.PURCHASED;
                    } else if (lastTransactionType == TransactionType.SALE) {
                        eventType = MarketEventType.RELISTED;
                    } else {
                        MarketEventType[] eventTypes = MarketEventType.values();
                        eventType =
                                eventTypes[ThreadLocalRandom.current().nextInt(eventTypes.length)];
                    }
                } else {
                    MarketEventType[] eventTypes = MarketEventType.values();
                    eventType = eventTypes[ThreadLocalRandom.current().nextInt(eventTypes.length)];
                }
            } else {
                MarketEventType[] eventTypes = MarketEventType.values();
                eventType = eventTypes[ThreadLocalRandom.current().nextInt(eventTypes.length)];
            }

            MarketInfoStoreDto marketInfoDto =
                    new MarketInfoStoreDto(newPrice, item.getId(), LocalDateTime.now(), eventType);

            int attempt = 0;
            while (attempt < MAX_RETRIES) {
                try {
                    marketInfoService.create(marketInfoDto);
                    LOGGER.debug(
                            "Створені ринкові дані для предмету {}: price={}, type={}",
                            item.getId(),
                            newPrice,
                            eventType);
                    return true;
                } catch (Exception e) {
                    attempt++;
                    if (attempt == MAX_RETRIES) {
                        LOGGER.error(
                                "Не вийшло зберегти ринкові дані предмету {} після {} спроб",
                                item.getId(),
                                MAX_RETRIES,
                                e);
                        return false;
                    }
                    Thread.sleep(100);
                }
            }
            return false;
        } catch (Exception e) {
            LOGGER.error("Помилка обробки предмету {}", item.getId(), e);
            return false;
        }
    }
}
