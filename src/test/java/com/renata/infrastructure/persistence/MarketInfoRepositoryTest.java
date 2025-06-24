package com.renata.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import com.renata.domain.entities.MarketInfo;
import com.renata.domain.enums.MarketEventType;
import com.renata.infrastructure.persistence.contract.MarketInfoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketInfoRepositoryTest {

    @Mock private MarketInfoRepository marketInfoRepository;

    private Faker faker;
    private MarketInfo marketInfo;
    private UUID itemId;
    private LocalDateTime timestamp;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        itemId = UUID.randomUUID();
        timestamp = LocalDateTime.now().minusDays(1);

        marketInfo = new MarketInfo();
        marketInfo.setId(UUID.randomUUID());
        marketInfo.setItemId(itemId);
        marketInfo.setPrice(faker.number().randomDouble(2, 100, 1000));
        marketInfo.setType(MarketEventType.LISTED);
        marketInfo.setTimestamp(timestamp);
    }

    @Test
    void findByItemId_ReturnsMarketInfoList() {
        when(marketInfoRepository.findByItemId(itemId)).thenReturn(List.of(marketInfo));

        List<MarketInfo> result = marketInfoRepository.findByItemId(itemId);

        assertEquals(1, result.size());
        assertEquals(itemId, result.get(0).getItemId());
        verify(marketInfoRepository).findByItemId(itemId);
    }

    @Test
    void findByEventType_ReturnsMarketInfoList() {
        MarketEventType type = MarketEventType.LISTED;
        when(marketInfoRepository.findByEventType(type)).thenReturn(List.of(marketInfo));

        List<MarketInfo> result = marketInfoRepository.findByEventType(type);

        assertEquals(1, result.size());
        assertEquals(type, result.get(0).getType());
        verify(marketInfoRepository).findByEventType(type);
    }

    @Test
    void findByDateRange_ReturnsMarketInfoList() {
        LocalDateTime from = LocalDateTime.now().minusDays(5);
        LocalDateTime to = LocalDateTime.now();

        when(marketInfoRepository.findByDateRange(from, to)).thenReturn(List.of(marketInfo));

        List<MarketInfo> result = marketInfoRepository.findByDateRange(from, to);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getTimestamp().isAfter(from));
        assertTrue(result.get(0).getTimestamp().isBefore(to.plusSeconds(1)));
        verify(marketInfoRepository).findByDateRange(from, to);
    }

    @Test
    void deleteOlderThan_ExecutesWithoutException() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        doNothing().when(marketInfoRepository).deleteOlderThan(cutoff);

        assertDoesNotThrow(() -> marketInfoRepository.deleteOlderThan(cutoff));
        verify(marketInfoRepository).deleteOlderThan(cutoff);
    }
}
