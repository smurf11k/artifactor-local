package com.renata.application.dto;

import com.renata.domain.enums.AntiqueType;
import com.renata.domain.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.nio.file.Path;

public record ItemStoreDto(
        @NotBlank(message = "can't be blank") String name,
        @NotBlank(message = "can't be blank") AntiqueType type,
        @Size(max = 512, message = "should be less than 512 chars") String description,
        @PastOrPresent(message = "can't be made in future") Integer productionYear,
        @Size(max = 256, message = "should be less than 256 chars")
                String country, // default: unknown
        @NotBlank(message = "can't be blank") ItemCondition condition,
        Path image) {}
