package com.moviebooking.model.dto.response;

import com.moviebooking.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.List;

public record BookingSummaryDto(
        Long id,
        BookingStatus status,
        BigDecimal totalAmount,
        String bookingTime,
        List<SeatDto> seats) {
}