package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddReviewRequest(
        @NotBlank String review,
        @Min(1) @Max(5) int rating) {
}