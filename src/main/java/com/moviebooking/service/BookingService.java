package com.moviebooking.service;

import com.moviebooking.model.dto.request.BookingRequest;
import com.moviebooking.model.dto.request.SeatSelectionRequest;
import com.moviebooking.model.dto.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {
    BookingResponse book(BookingRequest request, String username);

    void lockSeats(SeatSelectionRequest request, String username);

    BookingResponse modifyBooking(Long bookingId, BookingRequest request, String username);

    void cancelBooking(Long bookingId, String username);

    List<BookingResponse> groupBooking(List<BookingRequest> requests, String username);

    BookingResponse getBooking(Long bookingId, String username);

    Page<BookingResponse> getBookingsByUser(String username, Pageable pageable);

    java.util.List<String> getUnavailableSeats(Long showId);
}