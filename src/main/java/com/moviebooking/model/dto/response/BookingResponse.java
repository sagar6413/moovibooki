package com.moviebooking.model.dto.response;

import com.moviebooking.model.enums.BookingStatus;
import com.moviebooking.model.enums.SeatCategory;

import java.math.BigDecimal;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        Long userId,
        Long showId,
        String movieTitle,
        String theaterName,
        String showTime,
        List<String> seatNumbers,
        SeatCategory seatCategory,
        BigDecimal totalAmount,
        BookingStatus status,
        String paymentStatus) {
}