package com.renata.application.dto;

import com.renata.domain.entities.User.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserStoreDto(
        @NotBlank(message = "username can't be blank")
                @Size(min = 6, max = 34, message = "size should be from 4 to 34 chars")
                String username,
        @NotBlank(message = "can't be blank")
                @Size(min = 8, max = 72, message = "size should be from 8 to 72 chars")
                String password, // TODO: add regex pattern validation
        @NotBlank(message = "should not be blank")
                @Email(message = "should be a valid email")
                @Size(max = 128, message = "cant be more than 128 chars")
                String email,
        Role role) {}
