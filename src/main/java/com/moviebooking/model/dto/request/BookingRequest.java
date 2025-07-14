package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.PaymentMethod;
import com.moviebooking.model.enums.SeatCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookingRequest(
        @NotNull Long showId,
        @NotNull @Size(min = 1) List<String> seatNumbers,
        @NotNull SeatCategory seatCategory,
        @NotNull PaymentMethod paymentMethod,
        Long promoCodeId) {
}