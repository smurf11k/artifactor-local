package com.renata.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CollectionStoreDto(
        @NotNull(message = "ID мусить бути встановленим") UUID userId,
        @NotBlank(message = "Назва не може бути порожня")
                @Size(
                        min = 4,
                        max = 374,
                        message = "Поле назви має бути не менше 4 символів і не більше 374")
                String name) {}
