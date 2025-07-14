package com.moviebooking.model.dto.request;

import com.moviebooking.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull BigDecimal amount,
        @NotNull PaymentMethod paymentMethod,
        String transactionId) {
}