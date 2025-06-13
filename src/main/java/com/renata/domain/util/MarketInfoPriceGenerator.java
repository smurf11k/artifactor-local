package com.renata.domain.util;

import com.github.javafaker.Faker;
import com.renata.application.contract.ItemService;
import com.renata.application.contract.MarketInfoService;
import com.renata.application.dto.MarketInfoStoreDto;
import com.renata.domain.entities.Item;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarketInfoPriceGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketInfoPriceGenerator.class);
    private static final double MIN_INITIAL_PRICE = 10.0;
    private static final double MAX_INITIAL_PRICE = 100000.0;
    private static final int MAX_RETRIES = 3;

    @Autowired private ItemService itemService;
    @Autowired private MarketInfoService marketInfoService;
    private final Faker faker = new Faker();

    @PostConstruct
    public void generateMarketInfo() {
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
        } catch (Exception e) {
            // Silent catch to avoid logging
        }
    }

    private boolean processItem(Item item) {
        try {
            Optional<MarketInfo> latestMarketInfo =
                    marketInfoService.findLatestMarketInfo(item.getId());
            if (latestMarketInfo.isPresent()) {
                return false;
            }

            double newPrice =
                    faker.number()
                            .randomDouble(2, (int) MIN_INITIAL_PRICE, (int) MAX_INITIAL_PRICE);

            // Get all available event types and select one randomly
            MarketEventType[] eventTypes = MarketEventType.values();
            MarketEventType randomEventType =
                    eventTypes[ThreadLocalRandom.current().nextInt(eventTypes.length)];

            MarketInfoStoreDto marketInfoDto =
                    new MarketInfoStoreDto(
                            newPrice, item.getId(), LocalDateTime.now(), randomEventType);

            int attempt = 0;
            while (attempt < MAX_RETRIES) {
                try {
                    marketInfoService.create(marketInfoDto);
                    MarketInfo marketInfo = new MarketInfo();
                    marketInfo.setPrice(newPrice);
                    marketInfo.setItemId(item.getId());
                    marketInfo.setTimestamp(marketInfo.getTimestamp());
                    marketInfo.setType(randomEventType);
                    LOGGER.debug("Saving entity: {}", marketInfo);
                    return true;
                } catch (Exception e) {
                    attempt++;
                    if (attempt == MAX_RETRIES) {
                        return false;
                    }
                    Thread.sleep(100);
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
