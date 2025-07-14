package com.moviebooking.service.impl;

import com.moviebooking.exception.CustomExceptions;
import com.moviebooking.model.dto.request.BookingRequest;
import com.moviebooking.model.dto.request.SeatSelectionRequest;
import com.moviebooking.model.dto.response.BookingResponse;
import com.moviebooking.model.entity.*;
import com.moviebooking.model.enums.BookingStatus;
import com.moviebooking.model.enums.SeatCategory;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.BookingSeatRepository;
import com.moviebooking.repository.ShowRepository;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.service.BookingService;
import com.moviebooking.service.PaymentService;
import com.moviebooking.service.PromoService;
import com.moviebooking.util.EntityDtoMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {
    private static final long SEAT_LOCK_TIMEOUT_SEC = 120; // 2 minutes
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private final BookingRepository bookingRepository;
    private final ShowRepository showRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
    private final EntityDtoMapper mapper;
    private final PromoService promoService;

    @Override
    @CacheEvict(value = { "booking", "bookings" }, key = "#username")
    public BookingResponse book(BookingRequest request, String username) {
        log.info("Booking request by user: {} for show: {} with seats: {}", username, request.showId(),
                request.seatNumbers());

        // Validate input
        validateBookingRequest(request);

        // Fetch entities
        User user = getUserByUsername(username);
        Show show = getShowById(request.showId());

        // Check seat availability first (before locking)
        validateSeatsAvailable(show.getId(), request.seatNumbers(), null);

        // Process booking with distributed locking
        return processBookingWithLocks(request, user, show);
    }

    @Override
    @CacheEvict(value = { "booking", "bookings" }, key = "#username")
    public void lockSeats(SeatSelectionRequest request, String username) {
        Show show = getShowById(request.showId());

        // Use try-with-resources pattern for automatic lock management
        try (LockManager lockManager = new LockManager()) {
            for (String seat : request.seatNumbers()) {
                lockManager.acquireLock(getSeatLockKey(show.getId(), seat));
            }
            // Seats are locked - they will be automatically released when lockManager is
            // closed
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "booking", "bookings" }, key = "#username")
    public BookingResponse modifyBooking(Long bookingId, BookingRequest request, String username) {
        log.info("Modify booking request: {} by user: {}", bookingId, username);

        // Validate and fetch entities
        User user = getUserByUsername(username);
        Booking booking = getBookingById(bookingId);
        validateBookingOwnership(booking, user);

        // Get old and new seats
        List<String> oldSeats = getBookingSeats(bookingId);
        List<String> newSeats = request.seatNumbers();
        Set<String> allSeatsToLock = new HashSet<>();
        allSeatsToLock.addAll(oldSeats);
        if (newSeats != null) {
            allSeatsToLock.addAll(newSeats);
        }

        // Process modification with distributed locking
        return processBookingModificationWithLocks(booking, request, oldSeats, newSeats, allSeatsToLock);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "booking", "bookings" }, key = "#username")
    public void cancelBooking(Long bookingId, String username) {
        User user = getUserByUsername(username);
        Booking booking = getBookingById(bookingId);
        validateBookingOwnership(booking, user);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking {} cancelled by user: {}", bookingId, username);
    }

    @Override
    @Transactional
    public List<BookingResponse> groupBooking(List<BookingRequest> requests, String username) {
        User user = getUserByUsername(username);
        List<BookingResponse> responses = new ArrayList<>();
        for (BookingRequest request : requests) {
            responses.add(createBookingResponse(request, user));
        }
        return responses;
    }

    private BookingResponse createBookingResponse(BookingRequest request, User user) {
        // Validate input
        validateBookingRequest(request);

        // Fetch show
        Show show = getShowById(request.showId());

        // Check seat availability first (before locking)
        validateSeatsAvailable(show.getId(), request.seatNumbers(), null);

        // Process booking with distributed locking
        return processBookingWithLocks(request, user, show);
    }

    @Override
    @Cacheable(value = "booking", key = "#bookingId")
    public BookingResponse getBooking(Long bookingId, String username) {
        User user = getUserByUsername(username);
        Booking booking = getBookingById(bookingId);
        validateBookingOwnership(booking, user);

        return mapper.toBookingResponse(booking);
    }

    @Override
    public Page<BookingResponse> getBookingsByUser(String username, Pageable pageable) {
        User user = getUserByUsername(username);
        Page<Booking> bookings = bookingRepository.findByUserId(user.getId(), pageable);
        return bookings.map(mapper::toBookingResponse);
    }

    @Override
    public List<String> getUnavailableSeats(Long showId) {
        List<BookingSeat> seats = bookingSeatRepository.findByBooking_Show_Id(showId);
        return seats.stream()
                .filter(seat -> seat.getBooking().getStatus() != BookingStatus.CANCELLED)
                .map(BookingSeat::getSeatNumber)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private void validateBookingRequest(BookingRequest request) {
        if (request == null) {
            throw new CustomExceptions.InvalidRequestException("Booking request cannot be null");
        }
        if (request.seatNumbers() == null || request.seatNumbers().isEmpty()) {
            throw new CustomExceptions.InvalidRequestException("Seat numbers cannot be empty");
        }
        if (request.showId() == null) {
            throw new CustomExceptions.InvalidRequestException("Show ID cannot be null");
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + username));
    }

    private Show getShowById(Long showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new CustomExceptions.ShowNotFoundException("Show not found: " + showId));
    }

    private Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException("Booking not found: " + bookingId));
    }

    private void validateBookingOwnership(Booking booking, User user) {
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new CustomExceptions.AccessDeniedException("Not your booking");
        }
    }

    private List<String> getBookingSeats(Long bookingId) {
        return bookingSeatRepository.findByBookingId(bookingId).stream()
                .map(BookingSeat::getSeatNumber)
                .collect(Collectors.toList());
    }

    private void validateSeatsAvailable(Long showId, List<String> requestedSeats, Long excludeBookingId) {
        Set<String> unavailableSeats = bookingSeatRepository.findByBooking_Show_Id(showId)
                .stream()
                .filter(seat -> seat.getBooking()
                        .getStatus() != BookingStatus.CANCELLED)
                .filter(seat -> excludeBookingId == null || !seat.getBooking()
                        .getId()
                        .equals(excludeBookingId))
                .map(BookingSeat::getSeatNumber)
                .collect(Collectors.toSet());

        List<String> conflictingSeats = requestedSeats.stream()
                .filter(unavailableSeats::contains)
                .collect(Collectors.toList());

        if (!conflictingSeats.isEmpty()) {
            throw new CustomExceptions.SeatUnavailableException("Seats already booked: " + conflictingSeats);
        }
    }

    private BookingResponse processBookingWithLocks(BookingRequest request, User user, Show show) {
        try (LockManager lockManager = new LockManager()) {
            // Lock all seats
            for (String seat : request.seatNumbers()) {
                lockManager.acquireLock(getSeatLockKey(show.getId(), seat));
            }

            // Call transactional persistBooking
            Booking booking = persistBooking(request, user, show);

            log.info("Booking created: {} for user: {}", booking.getId(), user.getUsername());
            return mapper.toBookingResponse(booking);
        }
    }

    @Transactional
    private Booking persistBooking(BookingRequest request, User user, Show show) {
        // Double-check seat availability after locking
        validateSeatsAvailable(show.getId(), request.seatNumbers(), null);

        // Create booking
        Booking booking = createBooking(user, show, request);

        // Handle payment
        paymentService.processPayment(null, booking.getId(), user.getId());

        return booking;
    }

    private BookingResponse processBookingModificationWithLocks(Booking booking, BookingRequest request,
            List<String> oldSeats, List<String> newSeats,
            Set<String> allSeatsToLock) {
        try (LockManager lockManager = new LockManager()) {
            // Lock all seats
            for (String seat : allSeatsToLock) {
                lockManager.acquireLock(getSeatLockKey(booking.getShow().getId(), seat));
            }

            // Check new seats availability (excluding current booking)
            if (newSeats != null && !newSeats.isEmpty()) {
                validateSeatsAvailable(booking.getShow().getId(), newSeats, booking.getId());
                updateBookingSeats(booking, newSeats, request.seatCategory().name());
            }

            // Apply promo code if given
            if (request.promoCodeId() != null) {
                PromoCode promoCode = promoService.getPromoCodeById(request.promoCodeId());
                if (promoCode != null) {
                    BigDecimal discount = promoService.calculateDiscount(promoCode, booking);
                    booking.setTotalAmount(booking.getTotalAmount().subtract(discount));
                    booking.setPromoCode(promoCode);
                }
            }

            bookingRepository.save(booking);
            log.info("Booking {} modified by user: {}", booking.getId(), booking.getUser().getUsername());
            return mapper.toBookingResponse(booking);
        }
    }

    private Booking createBooking(User user, Show show, BookingRequest request) {
        BigDecimal totalAmount = show.getPrice().multiply(BigDecimal.valueOf(request.seatNumbers().size()));

        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .bookingTime(LocalDateTime.now())
                .totalAmount(totalAmount)
                .status(BookingStatus.CONFIRMED)
                .build();

        bookingRepository.save(booking);

        // Create booking seats in batch
        List<BookingSeat> bookingSeats = request.seatNumbers().stream()
                .map(seat -> BookingSeat.builder()
                        .booking(booking)
                        .seatNumber(seat)
                        .seatCategory(request.seatCategory())
                        .price(show.getPrice())
                        .build())
                .collect(Collectors.toList());

        bookingSeatRepository.saveAll(bookingSeats);

        // Apply promo code if given
        if (request.promoCodeId() != null) {
            PromoCode promoCode = promoService.getPromoCodeById(request.promoCodeId());
            if (promoCode != null) {
                BigDecimal discount = promoService.calculateDiscount(promoCode, booking);
                booking.setTotalAmount(booking.getTotalAmount().subtract(discount));
                booking.setPromoCode(promoCode);
            }
        }

        return booking;
    }

    private void updateBookingSeats(Booking booking, List<String> newSeats, String seatCategory) {
        // Remove old seats
        List<BookingSeat> oldBookingSeats = bookingSeatRepository.findByBookingId(booking.getId());
        bookingSeatRepository.deleteAll(oldBookingSeats);

        // Add new seats
        List<BookingSeat> newBookingSeats = newSeats.stream()
                .map((String seat) -> BookingSeat.builder()
                        .booking(booking)
                        .seatNumber(seat)
                        .seatCategory(SeatCategory.valueOf(seatCategory))
                        .price(booking.getShow()
                                .getPrice())
                        .build())
                .collect(Collectors.toList());

        bookingSeatRepository.saveAll(newBookingSeats);

        // Update total amount
        BigDecimal newTotal = booking.getShow().getPrice().multiply(BigDecimal.valueOf(newSeats.size()));
        booking.setTotalAmount(newTotal);

        // Apply promo code if given
        if (booking.getPromoCode() != null) {
            PromoCode promoCode = booking.getPromoCode();
            BigDecimal discount = promoService.calculateDiscount(promoCode, booking);
            booking.setTotalAmount(booking.getTotalAmount().subtract(discount));
        }
    }

    private String getSeatLockKey(Long showId, String seatNumber) {
        return "lock:show:" + showId + ":seat:" + seatNumber;
    }

    // Lock management utility
    private class LockManager implements AutoCloseable {
        private final List<RLock> acquiredLocks = new ArrayList<>();

        public void acquireLock(String lockKey) {
            try {
                RLock lock = redissonClient.getLock(lockKey);
                boolean locked = lock.tryLock(SEAT_LOCK_TIMEOUT_SEC, SEAT_LOCK_TIMEOUT_SEC, TimeUnit.SECONDS);
                if (!locked) {
                    throw new CustomExceptions.SeatLockedException("Seat is locked by another user");
                }
                acquiredLocks.add(lock);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CustomExceptions.SeatLockedException("Seat lock interrupted");
            }
        }

        @Override
        public void close() {
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            log.debug("Released {} seat locks", acquiredLocks.size());
        }
    }
}