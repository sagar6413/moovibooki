package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.Genre;
import com.moviebooking.model.enums.Language;
import jakarta.validation.constraints.Size;

public record MovieFilter(
        @Size(max = 100) String title,
        Genre genre,
        String rating,
        Language language,
        String releaseDate) {
}