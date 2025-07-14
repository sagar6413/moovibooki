package com.moviebooking.service.impl;

import com.moviebooking.model.dto.response.AnalyticsData;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.PaymentRepository;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.service.AnalyticsService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    public AnalyticsData getBookingsAnalytics() {
        var results = bookingRepository.countBookingsByMonth();
        return processAnalyticsResults(results);
    }

    @Override
    public AnalyticsData getRevenueAnalytics() {
        var results = paymentRepository.sumPaymentsByMonth();
        return processAnalyticsResults(results);
    }

    @Override
    public AnalyticsData getUsersAnalytics() {
        var results = userRepository.countUsersByMonth();
        return processAnalyticsResults(results);
    }

    private AnalyticsData processAnalyticsResults(java.util.List<Object[]> results) {
        java.util.List<String> labels = new java.util.ArrayList<>();
        java.util.List<Integer> data = new java.util.ArrayList<>();
        for (Object[] row : results) {
            labels.add((String) row[0]);
            data.add(((Number) row[1]).intValue());
        }
        return new AnalyticsData(labels, data);
    }
}