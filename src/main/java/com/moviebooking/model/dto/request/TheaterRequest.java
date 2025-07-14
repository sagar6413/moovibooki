package com.moviebooking.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record TheaterRequest(
        @NotBlank String name,
        @NotBlank String location,
        @NotNull @Size(min = 1) Set<String> amenities) {
}