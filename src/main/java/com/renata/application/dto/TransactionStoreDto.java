package com.renata.application.dto;

import com.renata.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionStoreDto(
        @NotBlank(message = "can't be blank") TransactionType type,
        @NotBlank(message = "can't be blank") UUID userId,
        @NotBlank(message = "can't be blank") UUID itemId,
        @NotBlank(message = "can't be blank") LocalDateTime timestamp) {}
