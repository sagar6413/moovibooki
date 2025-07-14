package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.SeatCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SeatSelectionRequest(
        @NotNull Long showId,
        @NotNull @Size(min = 1) List<String> seatNumbers,
        @NotNull SeatCategory seatCategory) {
}