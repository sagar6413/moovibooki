package com.moviebooking.model.dto.response;

import java.util.List;

public record AnalyticsData(
        List<String> labels,
        List<Integer> data) {
}