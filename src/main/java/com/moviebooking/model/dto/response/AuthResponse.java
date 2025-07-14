package com.moviebooking.model.dto.response;

import java.util.Set;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String username,
        String email,
        Set<String> roles) {
}