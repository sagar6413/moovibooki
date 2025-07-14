package com.moviebooking.model.dto.response;

import com.moviebooking.model.enums.SeatCategory;

public record SeatDto(
        Long id,
        String seatNumber,
        int row,
        int column,
        SeatCategory category) {
}