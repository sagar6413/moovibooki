package com.moviebooking.controller;

import com.moviebooking.model.dto.request.ScreenRequest;
import com.moviebooking.model.dto.request.TheaterFilter;
import com.moviebooking.model.dto.request.TheaterRequest;
import com.moviebooking.model.dto.request.UpdateAmenitiesRequest;
import com.moviebooking.model.dto.response.AnalyticsData;
import com.moviebooking.model.dto.response.ScreenResponse;
import com.moviebooking.model.dto.response.TheaterResponse;
import com.moviebooking.service.TheaterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
@Tag(name = "Theaters", description = "Theater and screen management")
public class TheaterController {
    private final TheaterService theaterService;

    @PostMapping
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Register a new theater", description = "Theater owner only. Registers a new theater.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TheaterResponse> registerTheater(@Valid @RequestBody TheaterRequest request,
            Principal principal) {
        TheaterResponse response = theaterService.registerTheater(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{theaterId}")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Partially update a theater", description = "Theater owner only. Partially updates theater details.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<TheaterResponse> updateTheater(
            @Parameter(description = "Theater ID") @PathVariable Long theaterId,
            @Valid @RequestBody TheaterRequest request, Principal principal) {
        TheaterResponse response = theaterService.updateTheater(theaterId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{theaterId}")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Delete a theater", description = "Theater owner only. Deletes a theater.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteTheater(@Parameter(description = "Theater ID") @PathVariable Long theaterId,
            Principal principal) {
        theaterService.deleteTheater(theaterId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{theaterId}")
    @Operation(summary = "Get theater details", description = "Get details of a specific theater.")
    public ResponseEntity<TheaterResponse> getTheater(
            @Parameter(description = "Theater ID") @PathVariable Long theaterId) {
        TheaterResponse response = theaterService.getTheater(theaterId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner/me")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Get theaters by owner", description = "Theater owner only. Get all theaters owned by a user.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<TheaterResponse>> getTheatersByOwner(Principal principal, Pageable pageable) {
        Page<TheaterResponse> responses = theaterService.getTheatersByOwner(principal.getName(), pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{theaterId}/screens")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Add a screen to a theater", description = "Theater owner only. Adds a new screen to a theater.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ScreenResponse> addScreen(@Parameter(description = "Theater ID") @PathVariable Long theaterId,
            @Valid @RequestBody ScreenRequest request, Principal principal) {
        ScreenResponse response = theaterService.addScreen(theaterId, request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/screens/{screenId}")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Update a screen", description = "Theater owner only. Updates screen details.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ScreenResponse> updateScreen(
            @Parameter(description = "Screen ID") @PathVariable Long screenId,
            @Valid @RequestBody ScreenRequest request, Principal principal) {
        ScreenResponse response = theaterService.updateScreen(screenId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/screens/{screenId}")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Delete a screen", description = "Theater owner only. Deletes a screen from a theater.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteScreen(@Parameter(description = "Screen ID") @PathVariable Long screenId,
            Principal principal) {
        theaterService.deleteScreen(screenId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{theaterId}/screens")
    @Operation(summary = "Get screens by theater", description = "Get all screens for a specific theater.")
    public ResponseEntity<Page<ScreenResponse>> getScreensByTheater(
            @Parameter(description = "Theater ID") @PathVariable Long theaterId,
            Pageable pageable) {
        Page<ScreenResponse> responses = theaterService.getScreensByTheater(theaterId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{theaterId}/amenities")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Update theater amenities", description = "Theater owner only. Updates amenities for a theater.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> updateAmenities(@Parameter(description = "Theater ID") @PathVariable Long theaterId,
            @RequestBody @Valid UpdateAmenitiesRequest request, Principal principal) {
        theaterService.updateAmenities(theaterId, request.amenities(), principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "List theaters with pagination", description = "List theaters with optional pagination.")
    public ResponseEntity<Page<TheaterResponse>> listTheaters(
            @ModelAttribute @Valid TheaterFilter filter,
            Pageable pageable) {
        Page<TheaterResponse> responses = theaterService.listTheaters(filter, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/owner/analytics/bookings")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Bookings analytics for owner", description = "Get bookings over time for all owned theaters.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AnalyticsData> getOwnerBookingsAnalytics(Principal principal) {
        AnalyticsData data = theaterService.getOwnerBookingsAnalytics(principal.getName());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/owner/analytics/revenue")
    @PreAuthorize("hasRole('THEATER_OWNER')")
    @Operation(summary = "Revenue analytics for owner", description = "Get revenue over time for all owned theaters.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<AnalyticsData> getOwnerRevenueAnalytics(Principal principal) {
        AnalyticsData data = theaterService.getOwnerRevenueAnalytics(principal.getName());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/{theaterId}/logo")
    public ResponseEntity<String> uploadTheaterLogo(@PathVariable Long theaterId,
            @RequestParam("file") MultipartFile file) {
        theaterService.uploadLogo(theaterId, file);
        return ResponseEntity.ok("Logo uploaded successfully");
    }
}