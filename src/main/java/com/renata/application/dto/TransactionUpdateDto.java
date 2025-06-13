package com.renata.application.dto;

import com.renata.domain.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionUpdateDto(
        @NotNull(message = "can't be null") UUID id,
        @NotNull(message = "can't be null") TransactionType type,
        @NotNull(message = "can't be null") UUID userId,
        @NotNull(message = "can't be null") UUID itemId,
        @NotNull(message = "can't be null") LocalDateTime timestamp) {}
