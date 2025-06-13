package com.renata.application.dto;

import com.renata.domain.enums.MarketEventType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record MarketInfoUpdateDto(
        @NotNull(message = "can't be null") UUID id,
        @NotNull(message = "can't be null") double price,
        @NotNull(message = "can't be null") UUID itemId,
        @NotNull(message = "can't be null") LocalDateTime timestamp,
        @NotNull(message = "can't be null") MarketEventType type) {}
