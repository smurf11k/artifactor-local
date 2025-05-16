package com.renata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CollectionStoreDto(
        @NotBlank(message = "can't be blank")
                @Size(min = 4, max = 374, message = "should be between 4 and 374 chars")
                String name,
        @NotBlank(message = "can't be blank") LocalDateTime createdAt) {}
