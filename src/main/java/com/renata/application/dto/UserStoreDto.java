package com.renata.application.dto;

import com.renata.domain.entities.User.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserStoreDto(
        @NotBlank(message = "Логін не може бути порожнім")
                @Size(
                        min = 6,
                        max = 34,
                        message = "Логін може містити не менше 6 символів і не більше 34")
                String username,
        @NotBlank(message = "Пароль не може бути порожнім")
                @Size(
                        min = 8,
                        max = 72,
                        message = "Пароль може містити не менше 8 символів і не більше 72")
                String password,
        @NotBlank(message = "Пошта не може бути порожньою")
                @Email(message = "Пошта має бути валідною")
                @Size(max = 128, message = "Пошта не може бути довшою за 128 символів")
                String email,
        Role role) {}
