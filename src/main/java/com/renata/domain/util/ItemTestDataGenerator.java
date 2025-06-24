package com.renata.domain.util;

import com.github.javafaker.Faker;
import com.renata.application.contract.ItemService;
import com.renata.application.dto.ItemStoreDto;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/** Утиліта яка створює тестові дані для елементів антикваріату. */
@Component
public class ItemTestDataGenerator {

    @Autowired private ItemService itemService;

    private final Random random = new Random();

    public void generateTestData() {
        Faker faker = new Faker();
        List<Path> imagePaths = loadItemImagePaths();

        for (int i = 0; i < 50; i++) {
            Path imagePath = null;
            if (!imagePaths.isEmpty() && random.nextBoolean()) {
                imagePath = imagePaths.get(random.nextInt(imagePaths.size()));
            }

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
                            imagePath);

            itemService.create(item, null, null);
        }
    }

    private List<Path> loadItemImagePaths() {
        List<Path> imagePaths = new ArrayList<>();
        try {
            Path tempDir = Files.createTempDirectory("item-images");

            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:images/items/*.{jpg,jpeg,png}");

            for (Resource resource : resources) {
                if (!resource.exists() || !resource.isReadable()) continue;

                String filename = Objects.requireNonNull(resource.getFilename());
                Path tempFile = tempDir.resolve(filename);

                Files.copy(
                        resource.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
                imagePaths.add(tempFile);
            }

        } catch (IOException e) {
            System.err.println("Failed to load item images: " + e.getMessage());
        }

        return imagePaths;
    }
}
