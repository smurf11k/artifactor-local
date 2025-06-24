package com.renata.application.dto;

import com.renata.domain.enums.MarketEventType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record MarketInfoStoreDto(
        @NotNull(message = "Ціна мусить бути встановлена") double price,
        @NotNull(message = "ID предмету мусить бути встановленим") UUID itemId,
        @NotNull(message = "Час мусить бути встановлений") LocalDateTime timestamp,
        @NotNull(message = "Тип мусить бути встановлений") MarketEventType type) {}
