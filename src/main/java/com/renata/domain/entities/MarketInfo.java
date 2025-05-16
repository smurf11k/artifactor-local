package com.renata.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

/** Сутність, що представляє ринкову ціну для конкретного антикваріату */
public class MarketInfo {
    private UUID id;
    private double currentPrice;
    private UUID itemId;
    private LocalDateTime lastUpdated;
    private String source;
}
