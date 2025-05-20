package com.renata.application.impl;

import com.renata.application.contract.ItemService;
import com.renata.application.dto.ItemStoreDto;
import com.renata.application.dto.ItemUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Item;
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

@Service
public class ItemServiceImpl implements ItemService {

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
        item.setName(itemStoreDto.name());
        item.setType(itemStoreDto.type());
        item.setDescription(itemStoreDto.description());
        item.setProductionYear(itemStoreDto.productionYear());
        item.setCountry(itemStoreDto.country());
        item.setCondition(itemStoreDto.condition());

        Path imagePath = itemStoreDto.image();
        if (image != null && imageName != null) {
            Path coverImagePath = fileStorageService.save(image, imageName, item.getId());
            item.setImagePath(coverImagePath.toString());
        } else if (imagePath != null) {
            try (InputStream pathStream = Files.newInputStream(imagePath)) {
                String derivedImageName = imagePath.getFileName().toString();
                Path coverImagePath =
                        fileStorageService.save(pathStream, derivedImageName, item.getId());
                item.setImagePath(coverImagePath.toString());
            } catch (Exception e) {
                throw new FileStorageException(
                        "Failed to process image from path: " + imagePath, e);
            }
        }

        persistenceContext.registerNew(item);
        persistenceContext.commit();
        return item;
    }

    @Override
    public Item update(UUID id, ItemUpdateDto itemUpdateDto, InputStream image, String imageName) {
        Set<jakarta.validation.ConstraintViolation<ItemUpdateDto>> violations =
                validator.validate(itemUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("item update", violations);
        }

        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isEmpty()) {
            throw new DatabaseAccessException("Item not found with id: " + id);
        }
        Item item = itemOpt.get();

        item.setName(itemUpdateDto.name());
        item.setType(itemUpdateDto.type());
        item.setDescription(itemUpdateDto.description());
        item.setProductionYear(itemUpdateDto.productionYear());
        item.setCountry(itemUpdateDto.country());
        item.setCondition(itemUpdateDto.condition());

        Path imagePath = itemUpdateDto.image();
        if (item.getImagePath() != null && (image != null || imagePath != null)) {
            fileStorageService.delete(item.getImagePath(), id);
        }

        if (image != null && imageName != null) {
            Path coverImagePath = fileStorageService.save(image, imageName, id);
            item.setImagePath(coverImagePath.toString());
        } else if (imagePath != null) {
            try (InputStream pathStream = Files.newInputStream(imagePath)) {
                String derivedImageName = imagePath.getFileName().toString();
                Path coverImagePath = fileStorageService.save(pathStream, derivedImageName, id);
                item.setImagePath(coverImagePath.toString());
            } catch (Exception e) {
                throw new FileStorageException(
                        "Failed to process image from path: " + imagePath, e);
            }
        } else {
            item.setImagePath(null);
        }

        persistenceContext.registerUpdated(id, item);
        persistenceContext.commit();
        return item;
    }

    @Override
    public void delete(UUID id) {
        Optional<Item> itemOpt = itemRepository.findById(id);
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();

            if (item.getImagePath() != null) {
                fileStorageService.delete(item.getImagePath(), id);
            }

            persistenceContext.registerDeleted(item);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return itemRepository.findById(id);
    }

    @Override
    public List<Item> findAll(int offset, int limit) {
        return itemRepository.findAll(offset, limit);
    }
}
