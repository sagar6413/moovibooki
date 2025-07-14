package com.moviebooking.model.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        String username,
        String review,
        int rating,
        LocalDateTime createdAt) {
}