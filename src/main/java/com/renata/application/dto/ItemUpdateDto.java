package com.renata.application.dto;

import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.nio.file.Path;
import java.util.UUID;

public record ItemUpdateDto(
        @NotNull(message = "Відсутній ідентифікатор антикваріату") UUID id,
        @NotBlank(message = "Назви не може бути пуста") String name,
        @NotNull(message = "Тип має бути встановлений") AntiqueType type,
        @Size(max = 512, message = "Опис не може бути довшим за 512 символів") String description,
        String productionYear, // maybe change to year range with two numbers
        @Size(max = 256, message = "Країна не може бути довшою за 256 символів") String country,
        @NotNull(message = "Стан має бути встановлений") ItemCondition condition,
        Path image) {}
