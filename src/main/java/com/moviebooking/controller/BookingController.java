package com.moviebooking.controller;

import com.moviebooking.model.dto.request.BookingRequest;
import com.moviebooking.model.dto.request.SeatSelectionRequest;
import com.moviebooking.model.dto.response.BookingResponse;
import com.moviebooking.service.BookingService;
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

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking and seat selection")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Book seats for a show", description = "Book one or more seats for a show. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookingRequest request, Principal principal) {
        BookingResponse response = bookingService.book(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/lock-seats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Lock seats for a show", description = "Lock seats for a show before booking. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> lockSeats(@Valid @RequestBody SeatSelectionRequest request, Principal principal) {
        bookingService.lockSeats(request, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Modify a booking", description = "Modify an existing booking. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookingResponse> modifyBooking(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId,
            @Valid @RequestBody BookingRequest request, Principal principal) {
        BookingResponse response = bookingService.modifyBooking(bookingId, request, principal.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Cancel a booking", description = "Cancel an existing booking. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> cancelBooking(@Parameter(description = "Booking ID") @PathVariable Long bookingId,
            Principal principal) {
        bookingService.cancelBooking(bookingId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/group")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Group booking", description = "Book multiple bookings as a group. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<BookingResponse>> groupBooking(@Valid @RequestBody List<BookingRequest> requests,
            Principal principal) {
        List<BookingResponse> responses = bookingService.groupBooking(requests, principal.getName());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Get booking details", description = "Get details of a specific booking. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<BookingResponse> getBooking(
            @Parameter(description = "Booking ID") @PathVariable Long bookingId,
            Principal principal) {
        BookingResponse response = bookingService.getBooking(bookingId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Get bookings by user", description = "Get all bookings for a specific user. User, Admin, or Theater Owner.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<BookingResponse>> getBookingsByUser(Principal principal, Pageable pageable) {
        Page<BookingResponse> responses = bookingService.getBookingsByUser(principal.getName(), pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/shows/{showId}/unavailable-seats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'THEATER_OWNER')")
    @Operation(summary = "Get unavailable seats for a show", description = "Fetch all booked/unavailable seats for a show.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<String>> getUnavailableSeats(
            @Parameter(description = "Show ID") @PathVariable Long showId) {
        List<String> seats = bookingService.getUnavailableSeats(showId);
        return ResponseEntity.ok(seats);
    }
}