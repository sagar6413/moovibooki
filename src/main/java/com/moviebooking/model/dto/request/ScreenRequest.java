package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.ScreenCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScreenRequest(
        @NotBlank String name,
        @NotNull int totalSeats,
        @NotNull ScreenCategory category) {
}