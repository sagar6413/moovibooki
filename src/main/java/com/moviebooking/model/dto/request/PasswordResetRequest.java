package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Reset token is required") String resetToken,
        @NotBlank(message = "New password is required") String newPassword) {
}