package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String password,
        @NotNull RoleType role) {
}