package com.renata.application.impl;

import com.renata.application.contract.ItemService;
import com.renata.application.dto.ItemStoreDto;
import com.renata.application.dto.ItemUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.file.FileStorageService;
import com.renata.infrastructure.file.exception.FileStorageException;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Реалізація сервісу для управління сутностями антикваріату, включаючи операції з файлами картинок.
 */
@Service
final class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;
    private final Validator validator;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            PersistenceContext persistenceContext,
            FileStorageService fileStorageService,
            Validator validator) {
        this.itemRepository = itemRepository;
        this.persistenceContext = persistenceContext;
        this.fileStorageService = fileStorageService;
        this.validator = validator;
    }

    @Override
    public Item create(ItemStoreDto itemStoreDto, InputStream image, String imageName) {
        Set<ConstraintViolation<ItemStoreDto>> violations = validator.validate(itemStoreDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("item creation", violations);
        }

        Item item = new Item();
        item.setId(UUID.randomUUID());
        setItemProperties(
                item,
                itemStoreDto.name(),
                itemStoreDto.type(),
                itemStoreDto.description(),
                itemStoreDto.productionYear(),
                itemStoreDto.country(),
                itemStoreDto.condition());

        validateAndProcessImage(item, itemStoreDto.image(), image, imageName, item.getId());

        persistenceContext.registerNew(item);
        persistenceContext.commit();
        return item;
    }

    @Override
    public Item update(ItemUpdateDto itemUpdateDto, InputStream image, String imageName) {
        Set<ConstraintViolation<ItemUpdateDto>> violations = validator.validate(itemUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("item update", violations);
        }

        UUID dtoId = itemUpdateDto.id();
        Item item =
                itemRepository
                        .findById(dtoId)
                        .orElseThrow(
                                () ->
                                        new DatabaseAccessException(
                                                "Предмет не знайдено з таким id: " + dtoId));

        setItemProperties(
                item,
                itemUpdateDto.name(),
                itemUpdateDto.type(),
                itemUpdateDto.description(),
                itemUpdateDto.productionYear(),
                itemUpdateDto.country(),
                itemUpdateDto.condition());

        validateAndProcessImage(item, itemUpdateDto.image(), image, imageName, dtoId);

        persistenceContext.registerUpdated(dtoId, item);
        persistenceContext.commit();
        return item;
    }

    @Override
    public void delete(UUID id) {
        itemRepository
                .findById(id)
                .ifPresent(
                        item -> {
                            if (item.getImagePath() != null) {
                                fileStorageService.delete(item.getImagePath(), id);
                            }
                            persistenceContext.registerDeleted(item);
                            persistenceContext.commit();
                        });
    }

    private void validateAndProcessImage(
            Item item, Path imagePath, InputStream image, String imageName, UUID itemId) {
        if (item.getImagePath() != null && (image != null || imagePath != null)) {
            fileStorageService.delete(item.getImagePath(), itemId);
        }

        if (image != null && imageName != null) {
            Path coverImagePath = fileStorageService.save(image, imageName, itemId);
            item.setImagePath(coverImagePath.toString());
        } else if (imagePath != null) {
            try (InputStream pathStream = Files.newInputStream(imagePath)) {
                String derivedImageName = imagePath.getFileName().toString();
                Path coverImagePath = fileStorageService.save(pathStream, derivedImageName, itemId);
                item.setImagePath(coverImagePath.toString());
            } catch (Exception e) {
                throw new FileStorageException(
                        "Не вийшло обробити картинку з шляху: " + imagePath, e);
            }
        } else {
            item.setImagePath(null);
        }
    }

    private void setItemProperties(
            Item item,
            String name,
            AntiqueType type,
            String description,
            String productionYear,
            String country,
            ItemCondition condition) {
        item.setName(name);
        item.setType(type);
        item.setDescription(description);
        item.setProductionYear(productionYear);
        item.setCountry(country);
        item.setCondition(condition);
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> findAll(int offset, int limit) {
        return itemRepository.findAll(offset, limit);
    }

    @Override
    public List<Item> findByName(String name) {
        return itemRepository.findByName(name);
    }

    @Override
    public List<Item> findByType(AntiqueType type) {
        return itemRepository.findByType(type);
    }

    @Override
    public List<Item> findByCountry(String country) {
        return itemRepository.findByCountry(country);
    }

    @Override
    public List<Item> findByCondition(ItemCondition condition) {
        return itemRepository.findByCondition(condition);
    }

    @Override
    public List<Item> findItemsByCollectionId(UUID collectionId) {
        return itemRepository.findItemsByCollectionId(collectionId);
    }
}
