package com.moviebooking.model.dto.response;

import java.util.List;

public record BookingHistoryResponse(
        Long userId,
        List<BookingSummaryDto> bookings) {
}