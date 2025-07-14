package com.moviebooking.service;

import com.moviebooking.model.dto.response.AnalyticsData;

public interface AnalyticsService {
    AnalyticsData getBookingsAnalytics();

    AnalyticsData getRevenueAnalytics();

    AnalyticsData getUsersAnalytics();
}