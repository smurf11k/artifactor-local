package com.renata.application.contract;

import com.renata.application.dto.MarketInfoStoreDto;
import com.renata.application.dto.MarketInfoUpdateDto;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Сервіс для операцій з ринковою інформацією. */
public interface MarketInfoService {

    /**
     * Створення нової ринкової інформації.
     *
     * @param marketInfoStoreDto DTO для створення ринкової інформації
     * @return створена ринкова інформація
     */
    MarketInfo create(MarketInfoStoreDto marketInfoStoreDto);

    /**
     * Оновлення існуючої ринкової інформації.
     *
     * @param marketInfoUpdateDto DTO для оновлення ринкової інформації
     * @return оновлена ринкова інформація
     */
    MarketInfo update(MarketInfoUpdateDto marketInfoUpdateDto);

    /**
     * Видалення ринкової інформації за ідентифікатором.
     *
     * @param id ідентифікатор ринкової інформації
     */
    void delete(UUID id);

    /**
     * Пошук ринкової інформації за ідентифікатором.
     *
     * @param id ідентифікатор ринкової інформації
     * @return Optional з ринковою інформацією
     */
    Optional<MarketInfo> findById(UUID id);

    /**
     * Пошук всіх ринкових інформацій з пагінацією.
     *
     * @param offset зміщення
     * @param limit ліміт
     * @return список ринкової інформації
     */
    List<MarketInfo> findAll(int offset, int limit);

    /**
     * Пошук ринкової інформації за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return список ринкової інформації
     */
    List<MarketInfo> findByItemId(UUID itemId);

    /**
     * Пошук ринкової інформації за типом події.
     *
     * @param type тип ринкової події
     * @return список ринкової інформації
     */
    List<MarketInfo> findByEventType(MarketEventType type);

    /**
     * Пошук ринкової інформації за діапазоном дат.
     *
     * @param from початкова дата
     * @param to кінцева дата
     * @return список ринкової інформації
     */
    List<MarketInfo> findByDateRange(LocalDateTime from, LocalDateTime to);

    /**
     * Пошук останньої ринкової інформації за ідентифікатором антикваріату.
     *
     * @param itemId ідентифікатор антикваріату
     * @return Optional з останньою ринковою інформацією
     */
    Optional<MarketInfo> findLatestMarketInfo(UUID itemId);

    /**
     * Deletes market info entries older than the specified date.
     *
     * @param olderThan the cutoff date
     */
    void deleteOlderThan(LocalDateTime olderThan);
}
