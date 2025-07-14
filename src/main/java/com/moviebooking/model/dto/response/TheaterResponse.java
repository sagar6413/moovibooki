package com.moviebooking.model.dto.response;

import java.util.List;
import java.util.Set;

public record TheaterResponse(
        Long theaterId,
        String name,
        String location,
        Set<String> amenities,
        Long ownerId,
        List<ScreenSummary> screens) {
}