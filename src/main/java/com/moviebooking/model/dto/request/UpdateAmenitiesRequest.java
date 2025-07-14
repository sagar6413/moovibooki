package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAmenitiesRequest(
        @NotBlank String amenities) {
}