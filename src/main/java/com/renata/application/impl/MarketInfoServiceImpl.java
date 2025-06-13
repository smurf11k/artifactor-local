package com.renata.application.impl;

import com.renata.application.contract.MarketInfoService;
import com.renata.application.dto.MarketInfoStoreDto;
import com.renata.application.dto.MarketInfoUpdateDto;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import com.renata.infrastructure.persistence.PersistenceContext;
import com.renata.infrastructure.persistence.contract.MarketInfoRepository;
import com.renata.infrastructure.persistence.exception.DatabaseAccessException;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
final class MarketInfoServiceImpl implements MarketInfoService {

    private final MarketInfoRepository marketInfoRepository;
    private final PersistenceContext persistenceContext;
    private final Validator validator;

    public MarketInfoServiceImpl(
            MarketInfoRepository marketInfoRepository,
            PersistenceContext persistenceContext,
            Validator validator) {
        this.marketInfoRepository = marketInfoRepository;
        this.persistenceContext = persistenceContext;
        this.validator = validator;
    }

    @Override
    public MarketInfo create(MarketInfoStoreDto marketInfoStoreDto) {
        MarketInfo marketInfo =
                MarketInfo.builder()
                        .id(UUID.randomUUID())
                        .price(marketInfoStoreDto.price())
                        .itemId(marketInfoStoreDto.itemId())
                        .type(marketInfoStoreDto.type())
                        .timestamp(
                                marketInfoStoreDto.timestamp() != null
                                        ? marketInfoStoreDto.timestamp()
                                        : LocalDateTime.now())
                        .build();

        persistenceContext.registerNew(marketInfo);
        persistenceContext.commit();
        return marketInfo;
    }

    @Override
    public MarketInfo update(UUID id, MarketInfoUpdateDto marketInfoUpdateDto) {
        Set<jakarta.validation.ConstraintViolation<MarketInfoUpdateDto>> violations =
                validator.validate(marketInfoUpdateDto);
        if (!violations.isEmpty()) {
            throw ValidationException.create("market info update", violations);
        }

        if (!id.equals(marketInfoUpdateDto.id())) {
            throw ValidationException.create(
                    "ID mismatch: provided ID does not match DTO ID", Set.of());
        }

        Optional<MarketInfo> marketInfoOpt = marketInfoRepository.findById(id);
        if (marketInfoOpt.isEmpty()) {
            throw new DatabaseAccessException("MarketInfo not found with id: " + id);
        }
        MarketInfo marketInfo = marketInfoOpt.get();

        marketInfo.setPrice(marketInfoUpdateDto.price());
        marketInfo.setItemId(marketInfoUpdateDto.itemId());
        marketInfo.setType(marketInfoUpdateDto.type());
        marketInfo.setTimestamp(marketInfoUpdateDto.timestamp());

        persistenceContext.registerUpdated(id, marketInfo);
        persistenceContext.commit();
        return marketInfo;
    }

    @Override
    public void delete(UUID id) {
        Optional<MarketInfo> marketInfoOpt = marketInfoRepository.findById(id);
        if (marketInfoOpt.isPresent()) {
            MarketInfo marketInfo = marketInfoOpt.get();
            persistenceContext.registerDeleted(marketInfo);
            persistenceContext.commit();
        }
    }

    @Override
    public Optional<MarketInfo> findById(UUID id) {
        return marketInfoRepository.findById(id);
    }

    @Override
    public List<MarketInfo> findAll(int offset, int limit) {
        return marketInfoRepository.findAll(offset, limit);
    }

    @Override
    public List<MarketInfo> findByItemId(UUID itemId) {
        return marketInfoRepository.findByItemId(itemId);
    }

    @Override
    public List<MarketInfo> findByEventType(MarketEventType type) {
        return marketInfoRepository.findByEventType(type);
    }

    @Override
    public List<MarketInfo> findByDateRange(LocalDateTime from, LocalDateTime to) {
        return marketInfoRepository.findByDateRange(from, to);
    }

    @Override
    public Optional<MarketInfo> findLatestMarketInfo(UUID itemId) {
        List<MarketInfo> marketInfos = marketInfoRepository.findByItemId(itemId);
        return marketInfos.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .findFirst();
    }

    @Override
    public void deleteOlderThan(LocalDateTime olderThan) {
        marketInfoRepository.deleteOlderThan(olderThan);
        persistenceContext.commit();
    }
}
