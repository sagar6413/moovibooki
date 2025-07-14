package com.moviebooking.model.dto.response;

import java.util.List;
import java.util.Set;

public record UserProfileResponse(
        Long userId,
        String username,
        String email,
        Set<String> roles,
        boolean enabled,
        boolean emailVerified,
        List<BookingSummaryDto> bookingHistory) {
}