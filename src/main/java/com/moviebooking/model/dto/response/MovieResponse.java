package com.moviebooking.model.dto.response;

import com.moviebooking.model.enums.Genre;
import com.moviebooking.model.enums.Language;

import java.util.List;
import java.util.Set;

public record MovieResponse(
        Long movieId,
        String title,
        Genre genre,
        int duration,
        String rating,
        Set<ActorDto> cast,
        String synopsis,
        String posterUrl,
        String releaseDate,
        Language language,
        List<ReviewResponse> reviews) {
}