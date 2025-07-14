package com.moviebooking.service;

import com.moviebooking.model.dto.request.PaymentRequest;
import com.moviebooking.model.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest request, Long bookingId, Long userId);

    void refundPayment(Long paymentId, Long userId);

    PaymentResponse getPaymentStatus(Long paymentId, Long userId);

    List<PaymentResponse> getPaymentHistory(Long userId);
}