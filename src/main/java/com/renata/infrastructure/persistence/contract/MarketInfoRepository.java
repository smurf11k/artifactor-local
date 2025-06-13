package com.renata.infrastructure.persistence.contract;

import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import com.renata.infrastructure.persistence.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Інтерфейс для операцій з ринковою інформацією в базі даних. */
public interface MarketInfoRepository extends Repository<MarketInfo, UUID> {

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
     * Deletes market info entries older than the specified date.
     *
     * @param olderThan the cutoff date
     */
    void deleteOlderThan(LocalDateTime olderThan);
}
