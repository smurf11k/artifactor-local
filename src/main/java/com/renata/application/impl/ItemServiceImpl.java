package com.renata.application.impl;

import com.renata.application.contract.ItemService;
import com.renata.domain.entities.Item;
import com.renata.infrastructure.file.FileStorageService;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final PersistenceContext persistenceContext;
    private final FileStorageService fileStorageService;

    public ItemServiceImpl(
            ItemRepository itemRepository,
            PersistenceContext persistenceContext,
            FileStorageService fileStorageService) {
        this.itemRepository = itemRepository;
        this.persistenceContext = persistenceContext;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public Item create(Item item, InputStream image, String imageName) {
        if (item.getId() == null) {
            item.setId(UUID.randomUUID());
        }

        if (image != null && imageName != null) {
            Path coverImagePath = fileStorageService.save(image, imageName, item.getId());
            item.setImagePath(coverImagePath.toString());
        }

        persistenceContext.registerNew(item);
        persistenceContext.commit();
        return item;
    }

    @Override
    public Item update(UUID id, Item item, InputStream image, String imageName) {
        item.setId(id);

        if (item.getImagePath() != null && image != null && imageName != null) {
            fileStorageService.delete(item.getImagePath(), id);
        }

        if (image != null && imageName != null) {
            Path coverImagePath = fileStorageService.save(image, imageName, id);
            item.setImagePath(coverImagePath.toString());
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
