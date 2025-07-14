package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Verification code is required") String verificationCode) {
}