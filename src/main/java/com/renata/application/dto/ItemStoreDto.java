package com.renata.application.dto;

import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.nio.file.Path;

public record ItemStoreDto(
        @NotBlank(message = "Назва не може бути порожня") String name,
        @NotNull(message = "Тип має бути встановлений") AntiqueType type,
        @Size(max = 512, message = "Опис не може бути довшим за 512 символів") String description,
        String productionYear,
        @Size(max = 256, message = "Назва країни не може бути довшою за 256 символів")
                String country,
        @NotNull(message = "Стан має бути встановлений") ItemCondition condition,
        Path image) {}
