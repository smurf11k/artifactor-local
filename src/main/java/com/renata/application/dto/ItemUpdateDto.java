package com.renata.application.dto;

import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.nio.file.Path;
import java.util.UUID;

public record ItemUpdateDto(
        @NotBlank(message = "Відсутній ідентифікатор антикваріату") UUID id,
        @NotBlank(message = "can't be blank") String name,
        @NotNull(message = "can't be null") AntiqueType type,
        @Size(max = 512, message = "should be less than 512 chars") String description,
        @NotBlank(message = "can't be blank") String productionYear,
        @Size(max = 256, message = "should be less than 256 chars") String country,
        @NotNull(message = "can't be null") ItemCondition condition,
        Path image) {}
