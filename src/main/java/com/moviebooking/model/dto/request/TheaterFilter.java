package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.Size;

public record TheaterFilter(
        @Size(max = 100) String name,
        String location,
        String amenity) {
}