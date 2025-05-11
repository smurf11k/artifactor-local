package com.renata.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

public class MarketInfo {
    private UUID id;
    private double currentPrice;
    private UUID itemId;
    private LocalDateTime lastUpdated;
    private String source;

}
