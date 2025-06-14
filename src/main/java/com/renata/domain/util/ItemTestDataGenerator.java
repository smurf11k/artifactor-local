package com.renata.domain.util;

import com.github.javafaker.Faker;
import com.renata.application.contract.ItemService;
import com.renata.application.dto.ItemStoreDto;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Utility class to generate test data for items. */
@Component
public class ItemTestDataGenerator {

    @Autowired private ItemService itemService;

    public void generateTestData() {
        Faker faker = new Faker();
        for (int i = 0; i < 50; i++) {
            ItemStoreDto item =
                    new ItemStoreDto(
                            faker.commerce().productName(),
                            AntiqueType.values()[
                                    faker.random().nextInt(AntiqueType.values().length)],
                            faker.lorem().sentence(),
                            String.valueOf(faker.number().numberBetween(1800, 2023)),
                            faker.address().country(),
                            ItemCondition.values()[
                                    faker.random().nextInt(ItemCondition.values().length)],
                            null // TODO: when choosing an image copy it to the items folder and use
                            // a relative path instead of full path
                            );
            itemService.create(item, null, null);
        }
    }
}
