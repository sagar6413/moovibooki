package com.moviebooking.controller;

import com.moviebooking.model.dto.response.AnalyticsData;
import com.moviebooking.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin dashboard and analytics")
public class AdminController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin dashboard", description = "View admin analytics and management dashboard.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> dashboard() {
        return ResponseEntity.ok("Admin dashboard - analytics and management endpoints coming soon.");
    }

    @GetMapping("/analytics/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bookings analytics", description = "Get bookings over time for admin analytics.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AnalyticsData> getBookingsAnalytics() {
        AnalyticsData data = analyticsService.getBookingsAnalytics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/analytics/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revenue analytics", description = "Get revenue over time for admin analytics.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AnalyticsData> getRevenueAnalytics() {
        AnalyticsData data = analyticsService.getRevenueAnalytics();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/analytics/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "User growth analytics", description = "Get user growth over time for admin analytics.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AnalyticsData> getUsersAnalytics() {
        AnalyticsData data = analyticsService.getUsersAnalytics();
        return ResponseEntity.ok(data);
    }
}