package com.moviebooking.util;

import com.moviebooking.model.dto.response.*;
import com.moviebooking.model.entity.*;
import com.moviebooking.model.enums.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class EntityDtoMapper {
        /**
         * Movie -> MovieResponse (flat mapping, service must provide all required data)
         */
        public MovieResponse toMovieResponse(
                        Long movieId,
                        String title,
                        Genre genre,
                        int duration,
                        String rating,
                        Set<ActorDto> cast,
                        String synopsis,
                        String posterUrl,
                        String releaseDate,
                        Language language,
                        List<ReviewResponse> reviews) {
                return new MovieResponse(
                                movieId,
                                title,
                                genre,
                                duration,
                                rating,
                                cast,
                                synopsis,
                                posterUrl,
                                releaseDate,
                                language,
                                reviews);
        }

        public MovieResponse toMovieResponse(Movie movie, List<ReviewResponse> reviews) {
                if (movie == null)
                        throw new RuntimeException("Movie not found");
                Set<ActorDto> cast = movie.getCast() == null ? Set.of()
                                : movie.getCast().stream()
                                                .map(actor -> toActorDto(actor.getId(), actor.getName(),
                                                                actor.getProfileImageUrl()))
                                                .collect(java.util.stream.Collectors.toSet());
                return toMovieResponse(
                                movie.getId(),
                                movie.getTitle(),
                                movie.getGenre(),
                                movie.getDuration(),
                                movie.getRating(),
                                cast,
                                movie.getSynopsis(),
                                movie.getPosterUrl(),
                                movie.getReleaseDate() != null ? movie.getReleaseDate().toString() : null,
                                movie.getLanguage(),
                                reviews);
        }

        /**
         * Booking -> BookingSummaryDto (flat mapping)
         */
        public BookingSummaryDto toBookingSummaryDto(
                        Long id,
                        BookingStatus status,
                        BigDecimal totalAmount,
                        String bookingTime,
                        List<SeatDto> seats) {
                return new BookingSummaryDto(id, status, totalAmount, bookingTime,
                                seats == null ? Collections.emptyList() : seats);
        }

        /**
         * Seat/BookingSeat -> SeatDto (flat mapping)
         */
        public SeatDto toSeatDto(
                        Long id,
                        String seatNumber,
                        int row,
                        int column,
                        SeatCategory category) {
                return new SeatDto(id, seatNumber, row, column, category);
        }

        /**
         * User -> UserProfileResponse (flat mapping)
         */
        public UserProfileResponse toUserProfileResponse(
                        Long userId,
                        String username,
                        String email,
                        Set<String> roles,
                        boolean enabled,
                        boolean emailVerified,
                        List<BookingSummaryDto> bookingHistory) {
                return new UserProfileResponse(userId, username, email, roles == null ? Collections.emptySet() : roles,
                                enabled,
                                emailVerified, bookingHistory == null ? Collections.emptyList() : bookingHistory);
        }

        /**
         * Payment -> PaymentResponse (flat mapping)
         */
        public PaymentResponse toPaymentResponse(
                        Long paymentId,
                        Long bookingId,
                        BigDecimal amount,
                        PaymentStatus status,
                        PaymentMethod paymentMethod,
                        String transactionId,
                        String paymentTime) {
                return new PaymentResponse(paymentId, bookingId, amount, status, paymentMethod, transactionId,
                                paymentTime);
        }

        public PaymentResponse toPaymentResponse(Payment payment) {
                return toPaymentResponse(
                                payment.getId(),
                                payment.getBooking() != null ? payment.getBooking().getId() : null,
                                payment.getAmount(),
                                payment.getStatus(),
                                payment.getPaymentMethod(),
                                payment.getTransactionId(),
                                payment.getPaymentTime() != null ? payment.getPaymentTime().toString() : null);
        }

        /**
         * Screen -> ScreenResponse (flat mapping)
         */
        public ScreenResponse toScreenResponse(
                        Long screenId,
                        String name,
                        String layout,
                        ScreenCategory category,
                        Long theaterId) {
                return new ScreenResponse(screenId, name, layout, category, theaterId);
        }

        public ScreenResponse toScreenResponse(Screen screen) {
                return toScreenResponse(
                                screen.getId(),
                                screen.getName(),
                                screen.getLayout(),
                                screen.getCategory(),
                                screen.getTheater() != null ? screen.getTheater().getId() : null);
        }

        /**
         * Theater -> TheaterResponse (flat mapping)
         */
        public TheaterResponse toTheaterResponse(
                        Long theaterId,
                        String name,
                        String location,
                        Set<String> amenities,
                        Long ownerId,
                        List<ScreenSummary> screens) {
                return new TheaterResponse(theaterId, name, location,
                                amenities == null ? Collections.emptySet() : amenities,
                                ownerId, screens == null ? Collections.emptyList() : screens);
        }

        public TheaterResponse toTheaterResponse(Theater theater) {
                Set<String> amenities = theater.getAmenities() == null ? Set.of() : theater.getAmenities();
                List<ScreenSummary> screens = theater.getScreens() == null ? List.of()
                                : theater.getScreens().stream()
                                                .map(this::toScreenSummary)
                                                .collect(java.util.stream.Collectors.toList());
                return toTheaterResponse(
                                theater.getId(),
                                theater.getName(),
                                theater.getLocation(),
                                amenities,
                                theater.getOwner() != null ? theater.getOwner().getId() : null,
                                screens);
        }

        /**
         * Screen -> ScreenSummary (flat mapping)
         */
        public ScreenSummary toScreenSummary(
                        Long screenId,
                        String name,
                        String category) {
                return new ScreenSummary(screenId, name, category);
        }

        public ScreenSummary toScreenSummary(Screen screen) {
                return toScreenSummary(
                                screen.getId(),
                                screen.getName(),
                                screen.getCategory() != null ? screen.getCategory().name() : null);
        }

        /**
         * BookingHistoryResponse (flat mapping)
         */
        public BookingHistoryResponse toBookingHistoryResponse(Long userId, List<BookingSummaryDto> bookings) {
                return new BookingHistoryResponse(userId, bookings == null ? Collections.emptyList() : bookings);
        }

        /**
         * AuthResponse (flat mapping)
         */
        public AuthResponse toAuthResponse(
                        String accessToken,
                        String refreshToken,
                        Long userId,
                        String username,
                        String email,
                        Set<String> roles) {
                return new AuthResponse(accessToken, refreshToken, userId, username, email,
                                roles == null ? Collections.emptySet() : roles);
        }

        /**
         * AnalyticsData (flat mapping)
         */
        public AnalyticsData toAnalyticsData(List<String> labels, List<Integer> data) {
                return new AnalyticsData(labels == null ? Collections.emptyList() : labels,
                                data == null ? Collections.emptyList() : data);
        }

        public BookingResponse toBookingResponse(
                        Long bookingId,
                        Long userId,
                        Long showId,
                        String movieTitle,
                        String theaterName,
                        String showTime,
                        java.util.List<String> seatNumbers,
                        SeatCategory seatCategory,
                        BigDecimal totalAmount,
                        BookingStatus status,
                        com.moviebooking.model.enums.PaymentStatus paymentStatus) {
                return new BookingResponse(
                                bookingId,
                                userId,
                                showId,
                                movieTitle,
                                theaterName,
                                showTime,
                                seatNumbers,
                                seatCategory,
                                totalAmount,
                                status,
                                paymentStatus != null ? paymentStatus.name() : null);
        }

        public BookingResponse toBookingResponse(Booking booking) {
                if (booking == null)
                        return null;
                Long bookingId = booking.getId();
                Long userId = booking.getUser() != null ? booking.getUser().getId() : null;
                Long showId = booking.getShow() != null ? booking.getShow().getId() : null;
                String movieTitle = (booking.getShow() != null && booking.getShow().getMovie() != null)
                                ? booking.getShow().getMovie().getTitle()
                                : null;
                String theaterName = (booking.getShow() != null && booking.getShow().getScreen() != null
                                && booking.getShow().getScreen().getTheater() != null)
                                                ? booking.getShow().getScreen().getTheater().getName()
                                                : null;
                String showTime = booking.getShow() != null ? booking.getShow().getStartTime().toString() : null;
                java.util.List<String> seatNumbers = booking.getBookingSeats() != null
                                ? booking.getBookingSeats().stream().map(bs -> bs.getSeatNumber()).toList()
                                : java.util.Collections.emptyList();
                // Use the first seat's category if available
                SeatCategory seatCategory = (booking.getBookingSeats() != null && !booking.getBookingSeats().isEmpty())
                                ? booking.getBookingSeats().iterator().next().getSeatCategory()
                                : null;
                BigDecimal totalAmount = booking.getTotalAmount();
                BookingStatus status = booking.getStatus();
                PaymentStatus paymentStatus = booking.getPayment() != null ? booking.getPayment().getStatus() : null;
                return toBookingResponse(
                                bookingId,
                                userId,
                                showId,
                                movieTitle,
                                theaterName,
                                showTime,
                                seatNumbers,
                                seatCategory,
                                totalAmount,
                                status,
                                paymentStatus);
        }

        public ActorDto toActorDto(Long id, String name, String profileImageUrl) {
                return new ActorDto(id, name, profileImageUrl);
        }

        public ReviewResponse toReviewResponse(String username, String review, int rating,
                        java.time.LocalDateTime createdAt) {
                return new ReviewResponse(username, review, rating, createdAt);
        }

        // Request DTOs to Entities (where applicable)
        // Service must provide all required data (no deep entity traversal)
        // Add more mappings as needed
}