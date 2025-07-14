package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.Genre;
import com.moviebooking.model.enums.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record MovieRequest(
        @NotBlank String title,
        @NotNull Genre genre,
        @NotNull int duration,
        @NotBlank String rating,
        @NotNull @Size(min = 1) Set<Long> cast,
        @NotBlank String synopsis,
        @NotNull LocalDate releaseDate,
        @NotNull Language language) {
}