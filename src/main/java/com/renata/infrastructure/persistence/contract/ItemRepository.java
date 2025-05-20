package com.renata.infrastructure.persistence.contract;

import com.renata.domain.entities.Item;
import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import com.renata.infrastructure.persistence.Repository;
import java.util.List;
import java.util.UUID;

/** Інтерфейс репозиторію для специфічних операцій з антикваріатом. */
public interface ItemRepository extends Repository<Item, UUID> {

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
