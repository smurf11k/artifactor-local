package com.renata.application.contract;

import com.renata.application.dto.ItemStoreDto;
import com.renata.application.dto.ItemUpdateDto;
import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.file.exception.FileStorageException;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Інтерфейс для управління сутностями антикваріату, включаючи операції з файлами картинок. */
public interface ItemService {
    /**
     * Створює новий антикваріат та, за потреби, завантажує картинку.
     *
     * @param itemStoreDto DTO з даними для створення антикваріату
     * @param image потік даних картинки, може бути null
     * @param imageName ім'я файлу картинки, може бути null
     * @return створений антикваріат
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException якщо виникає помилка при роботі з файлами
     */
    Item create(ItemStoreDto itemStoreDto, InputStream image, String imageName);

    /**
     * Оновлює існуючий антикваріат та, за потреби, оновлює картинку.
     *
     * @param id ідентифікатор антикваріату для оновлення
     * @param itemUpdateDto DTO з даними для оновлення антикваріату
     * @param image потік даних нової картинки, може бути null
     * @param imageName ім'я файлу нової картинки, може бути null
     * @return оновлений антикваріат
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException якщо виникає помилка при роботі з файлами
     */
    Item update(UUID id, ItemUpdateDto itemUpdateDto, InputStream image, String imageName);

    /**
     * Видаляє антикваріат та всі пов'язані файли.
     *
     * @param id ідентифікатор антикваріату для видалення
     * @throws DatabaseAccessException якщо виникає помилка при роботі з базою даних
     * @throws FileStorageException якщо виникає помилка при роботі з файлами
     */
    void delete(UUID id);

    /**
     * Знаходить антикваріат за ідентифікатором.
     *
     * @param id ідентифікатор антикваріату
     * @return Optional з антикваріатом, якщо знайдено
     */
    Optional<Item> findById(UUID id);

    /**
     * Знаходить всі елементи антикваріату з пагінацією.
     *
     * @param offset зміщення для пагінації
     * @param limit кількість записів для отримання
     * @return список антикваріату
     */
    List<Item> findAll(int offset, int limit);

    /**
     * Пошук антикваріату за назвою.
     *
     * @param name назва антикваріату
     * @return список антикваріату
     */
    List<Item> findByName(String name);

    /**
     * Пошук антикваріату за типом.
     *
     * @param type тип антикваріату
     * @return список антикваріату
     */
    List<Item> findByType(AntiqueType type);

    /**
     * Пошук антикваріату за країною походження.
     *
     * @param country країна
     * @return список антикваріату
     */
    List<Item> findByCountry(String country);

    /**
     * Пошук антикваріату за його станом.
     *
     * @param condition стан антикваріату
     * @return список антикваріату
     */
    List<Item> findByCondition(ItemCondition condition);
}
