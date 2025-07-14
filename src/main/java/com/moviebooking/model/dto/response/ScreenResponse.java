package com.moviebooking.model.dto.response;

import com.moviebooking.model.enums.ScreenCategory;

public record ScreenResponse(
        Long screenId,
        String name,
        String layout,
        ScreenCategory category,
        Long theaterId) {
}