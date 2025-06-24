package com.renata.application.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.renata.application.dto.ItemStoreDto;
import com.renata.application.dto.ItemUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.file.FileStorageService;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.ItemRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ItemServiceImplTest {

    private ItemRepository itemRepository;
    private PersistenceContext persistenceContext;
    private FileStorageService fileStorageService;
    private Validator validator;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        persistenceContext = mock(PersistenceContext.class);
        fileStorageService = mock(FileStorageService.class);
        validator = mock(Validator.class);

        itemService =
                new ItemServiceImpl(
                        itemRepository, persistenceContext, fileStorageService, validator);
    }

    @Test
    void create_validDto_imageProvided_savesItemAndImage() throws Exception {
        ItemStoreDto dto = mock(ItemStoreDto.class);
        when(dto.name()).thenReturn("Name");
        when(dto.type()).thenReturn(AntiqueType.ANTIQUE);
        when(dto.description()).thenReturn("Desc");
        when(dto.productionYear()).thenReturn("1990");
        when(dto.country()).thenReturn("Country");
        when(dto.condition()).thenReturn(ItemCondition.FAIR);
        when(dto.image()).thenReturn(null);

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        InputStream imageStream = new ByteArrayInputStream(new byte[] {1, 2, 3});
        String imageName = "image.png";

        Path fakePath = Path.of("/fake/path/image.png");
        when(fileStorageService.save(any(InputStream.class), eq(imageName), any(UUID.class)))
                .thenReturn(fakePath);

        Item created = itemService.create(dto, imageStream, imageName);

        assertNotNull(created);
        assertEquals("Name", created.getName());
        assertEquals(AntiqueType.ANTIQUE, created.getType());
        assertEquals("Desc", created.getDescription());
        assertEquals("1990", created.getProductionYear());
        assertEquals("Country", created.getCountry());
        assertEquals(ItemCondition.FAIR, created.getCondition());
        assertEquals(fakePath.toString(), created.getImagePath());

        verify(persistenceContext).registerNew(created);
        verify(persistenceContext).commit();
        verify(fileStorageService).save(imageStream, imageName, created.getId());
    }

    @Test
    void create_invalidDto_throwsValidationException() {
        ItemStoreDto dto = mock(ItemStoreDto.class);
        Set<ConstraintViolation<ItemStoreDto>> violations = Set.of(mock(ConstraintViolation.class));
        when(validator.validate(dto)).thenReturn(violations);

        ValidationException ex =
                assertThrows(ValidationException.class, () -> itemService.create(dto, null, null));
        assertTrue(ex.getMessage().contains("item creation"));
    }

    @Test
    void update_existingItemWithNewImage_updatesAndSavesImage() throws Exception {
        UUID id = UUID.randomUUID();

        // Prepare DTO mock
        ItemUpdateDto dto = mock(ItemUpdateDto.class);
        when(dto.id()).thenReturn(id);
        when(dto.name()).thenReturn("UpdatedName");
        when(dto.type()).thenReturn(AntiqueType.ANTIQUE);
        when(dto.description()).thenReturn("UpdatedDesc");
        when(dto.productionYear())
                .thenReturn("2000"); // String is okay if productionYear is String in DTO
        when(dto.country()).thenReturn("UpdatedCountry");
        when(dto.condition()).thenReturn(ItemCondition.POOR);
        when(dto.image()).thenReturn(null); // no Path given

        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        Item existing = new Item();
        existing.setId(id);
        existing.setImagePath("/old/path/image.png");

        when(itemRepository.findById(id)).thenReturn(Optional.of(existing));

        InputStream newImageStream = new ByteArrayInputStream(new byte[] {4, 5, 6});
        String newImageName = "newImage.png";
        Path savedPath = Path.of("/new/path/image.png");

        doNothing().when(fileStorageService).delete(existing.getImagePath(), id);

        when(fileStorageService.save(newImageStream, newImageName, id)).thenReturn(savedPath);

        Item updated = itemService.update(dto, newImageStream, newImageName);

        assertEquals("UpdatedName", updated.getName());
        assertEquals(AntiqueType.ANTIQUE, updated.getType());
        assertEquals("UpdatedDesc", updated.getDescription());
        assertEquals("2000", updated.getProductionYear());
        assertEquals("UpdatedCountry", updated.getCountry());
        assertEquals(ItemCondition.POOR, updated.getCondition());
        assertEquals(savedPath.toString(), updated.getImagePath());

        verify(fileStorageService).delete("/old/path/image.png", id);

        verify(fileStorageService).save(newImageStream, newImageName, id);

        verify(persistenceContext).registerUpdated(id, updated);
        verify(persistenceContext).commit();
    }

    @Test
    void update_itemNotFound_throwsDatabaseAccessException() {
        UUID id = UUID.randomUUID();
        ItemUpdateDto dto = mock(ItemUpdateDto.class);
        when(dto.id()).thenReturn(id);
        when(validator.validate(dto)).thenReturn(Collections.emptySet());

        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        DatabaseAccessException ex =
                assertThrows(
                        DatabaseAccessException.class, () -> itemService.update(dto, null, null));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void delete_existingItemWithImage_deletesImageAndItem() {
        UUID id = UUID.randomUUID();
        Item item = new Item();
        item.setId(id);
        item.setImagePath("/some/path/image.png");

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        doNothing().when(fileStorageService).delete(item.getImagePath(), id);

        itemService.delete(id);

        verify(fileStorageService).delete(item.getImagePath(), id);
        verify(persistenceContext).registerDeleted(item);
        verify(persistenceContext).commit();
    }

    @Test
    void delete_existingItemWithoutImage_deletesItemOnly() {
        UUID id = UUID.randomUUID();
        Item item = new Item();
        item.setId(id);
        item.setImagePath(null);

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        itemService.delete(id);

        verify(fileStorageService, never()).delete(anyString(), any());
        verify(persistenceContext).registerDeleted(item);
        verify(persistenceContext).commit();
    }

    @Test
    void findById_callsRepository() {
        UUID id = UUID.randomUUID();
        itemService.findById(id);
        verify(itemRepository).findById(id);
    }

    @Test
    void findAll_callsRepository() {
        itemService.findAll(0, 10);
        verify(itemRepository).findAll(0, 10);
    }

    @Test
    void findByName_nullOrEmpty_callsFindAll() {
        itemService.findByName(null);
        itemService.findByName(" ");
        verify(itemRepository, times(2)).findAll(0, 100);
    }

    @Test
    void findByName_validName_callsFindByName() {
        String name = "testName";
        itemService.findByName(name);
        verify(itemRepository).findByName(name);
    }

    @Test
    void findByType_null_callsFindAll() {
        itemService.findByType(null);
        verify(itemRepository).findAll(0, 100);
    }

    @Test
    void findByType_valid_callsFindByType() {
        AntiqueType type = AntiqueType.ANTIQUE;
        itemService.findByType(type);
        verify(itemRepository).findByType(type);
    }

    @Test
    void findByCountry_nullOrEmpty_callsFindAll() {
        itemService.findByCountry(null);
        itemService.findByCountry(" ");
        verify(itemRepository, times(2)).findAll(0, 100);
    }

    @Test
    void findByCountry_valid_callsFindByCountry() {
        String country = "Italy";
        itemService.findByCountry(country);
        verify(itemRepository).findByCountry(country);
    }

    @Test
    void findByCondition_null_callsFindAll() {
        itemService.findByCondition(null);
        verify(itemRepository).findAll(0, 100);
    }

    @Test
    void findByCondition_valid_callsFindByCondition() {
        itemService.findByCondition(ItemCondition.EXCELLENT);
        verify(itemRepository).findByCondition(ItemCondition.EXCELLENT);
    }
}
