package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Email is required") String email) {
}