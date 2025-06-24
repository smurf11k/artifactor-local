package com.renata.application.dto;

import com.renata.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionStoreDto(
        @NotNull(message = "ID користувача мусить бути встановленим") UUID userId,
        @NotNull(message = "ID предмету мусить бути встановленим") UUID itemId,
        @NotNull(message = "Тип мусить бути встановленим") TransactionType type,
        @NotNull(message = "Дата мусить бути встановлена") LocalDateTime timestamp) {}
