package com.moviebooking.service.impl;

import com.moviebooking.exception.CustomExceptions;
import com.moviebooking.model.dto.request.ScreenRequest;
import com.moviebooking.model.dto.request.TheaterFilter;
import com.moviebooking.model.dto.request.TheaterRequest;
import com.moviebooking.model.dto.response.AnalyticsData;
import com.moviebooking.model.dto.response.ScreenResponse;
import com.moviebooking.model.dto.response.TheaterResponse;
import com.moviebooking.model.entity.Screen;
import com.moviebooking.model.entity.Theater;
import com.moviebooking.model.entity.User;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.ScreenRepository;
import com.moviebooking.repository.TheaterRepository;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.service.ImageService;
import com.moviebooking.service.TheaterService;
import com.moviebooking.util.EntityDtoMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TheaterServiceImpl implements TheaterService {
    private static final Logger log = LoggerFactory.getLogger(TheaterServiceImpl.class);
    private static final int ANALYTICS_MONTHS = 6;

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ImageService imageService;
    private final EntityDtoMapper mapper;

    @Override
    @Transactional
    @CachePut(value = "theater", key = "#result.theaterId")
    @CacheEvict(value = { "theaters", "screens" }, allEntries = true)
    public TheaterResponse registerTheater(TheaterRequest request, String username) {
        log.info("Registering theater: {} by user: {}", request.name(), username);

        User owner = findUserByUsername(username);
        Theater theater = buildTheater(request, owner);
        theater = theaterRepository.save(theater);

        log.info("Theater registered with ID: {}", theater.getId());
        return mapper.toTheaterResponse(theater);
    }

    @Override
    @Transactional
    @CachePut(value = "theater", key = "#theaterId")
    @CacheEvict(value = { "theaters", "screens" }, allEntries = true)
    public TheaterResponse updateTheater(Long theaterId, TheaterRequest request, String username) {
        log.info("Updating theater: {} by user: {}", theaterId, username);

        User owner = findUserByUsername(username);
        Theater theater = findTheaterById(theaterId);
        validateOwnership(theater, owner);

        updateTheaterFields(theater, request);
        theater = theaterRepository.save(theater);

        log.info("Theater updated: {}", theaterId);
        return mapper.toTheaterResponse(theater);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "theater", "theaters", "screens" }, allEntries = true)
    public void deleteTheater(Long theaterId, String username) {
        log.info("Deleting theater: {} by user: {}", theaterId, username);

        User owner = findUserByUsername(username);
        Theater theater = findTheaterById(theaterId);
        validateOwnership(theater, owner);

        theaterRepository.delete(theater);
        log.info("Theater deleted: {}", theaterId);
    }

    @Override
    @Cacheable(value = "theater", key = "#theaterId")
    public TheaterResponse getTheater(Long theaterId) {
        Theater theater = findTheaterById(theaterId);
        return mapper.toTheaterResponse(theater);
    }

    @Override
    public Page<TheaterResponse> getTheatersByOwner(String username, Pageable pageable) {
        User owner = findUserByUsername(username);
        Page<Theater> theaters = theaterRepository.findByOwnerId(owner.getId(), pageable);
        return theaters.map(mapper::toTheaterResponse);
    }

    @Override
    @Transactional
    @CachePut(value = "screen", key = "#result.screenId")
    @CacheEvict(value = "screens", allEntries = true)
    public ScreenResponse addScreen(Long theaterId, ScreenRequest request, String username) {
        log.info("Adding screen to theater: {} by user: {}", theaterId, username);

        User owner = findUserByUsername(username);
        Theater theater = findTheaterById(theaterId);
        validateOwnership(theater, owner);

        Screen screen = buildScreen(request, theater);
        screen = screenRepository.save(screen);

        log.info("Screen added with ID: {}", screen.getId());
        return mapper.toScreenResponse(screen);
    }

    @Override
    @Transactional
    @CachePut(value = "screen", key = "#screenId")
    @CacheEvict(value = "screens", allEntries = true)
    public ScreenResponse updateScreen(Long screenId, ScreenRequest request, String username) {
        log.info("Updating screen: {} by user: {}", screenId, username);

        User owner = findUserByUsername(username);
        Screen screen = findScreenById(screenId);
        validateScreenOwnership(screen, owner);

        updateScreenFields(screen, request);
        screen = screenRepository.save(screen);

        log.info("Screen updated: {}", screenId);
        return mapper.toScreenResponse(screen);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "screen", "screens" }, allEntries = true)
    public void deleteScreen(Long screenId, String username) {
        log.info("Deleting screen: {} by user: {}", screenId, username);

        User owner = findUserByUsername(username);
        Screen screen = findScreenById(screenId);
        validateScreenOwnership(screen, owner);

        screenRepository.delete(screen);
        log.info("Screen deleted: {}", screenId);
    }

    @Override
    public Page<ScreenResponse> getScreensByTheater(Long theaterId, Pageable pageable) {
        Page<Screen> screens = screenRepository.findByTheaterId(theaterId, pageable);
        return screens.map(mapper::toScreenResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "theater", "theaters" }, key = "#theaterId")
    public void updateAmenities(Long theaterId, String amenities, String username) {
        log.info("Updating amenities for theater: {} by user: {}", theaterId, username);

        User owner = findUserByUsername(username);
        Theater theater = findTheaterById(theaterId);
        validateOwnership(theater, owner);

        Set<String> amenitiesSet = parseAmenities(amenities);
        theater.setAmenities(amenitiesSet);
        theaterRepository.save(theater);

        log.info("Amenities updated for theater: {}", theaterId);
    }

    @Override
    public Page<TheaterResponse> listTheaters(TheaterFilter filter, Pageable pageable) {
        Specification<Theater> spec = buildTheaterSpecification(filter);
        Page<Theater> theaters = theaterRepository.findAll(spec, pageable);
        return theaters.map(mapper::toTheaterResponse);
    }

    @Override
    @Cacheable(value = "ownerBookingsAnalytics", key = "#username")
    public AnalyticsData getOwnerBookingsAnalytics(String username) {
        log.debug("Fetching bookings analytics for owner: {}", username);

        User owner = findUserByUsername(username);
        Map<String, Integer> monthToCount = generateMonthlyBookingCounts(owner.getId());

        return new AnalyticsData(
                List.copyOf(monthToCount.keySet()),
                List.copyOf(monthToCount.values()));
    }

    @Override
    @Cacheable(value = "ownerRevenueAnalytics", key = "#username")
    public AnalyticsData getOwnerRevenueAnalytics(String username) {
        log.debug("Fetching revenue analytics for owner: {}", username);

        User owner = findUserByUsername(username);
        Map<String, Integer> monthToRevenue = generateMonthlyRevenueCounts(owner.getId());

        return new AnalyticsData(
                List.copyOf(monthToRevenue.keySet()),
                List.copyOf(monthToRevenue.values()));
    }

    @Override
    public Theater getTheaterById(Long id) {
        return findTheaterById(id);
    }

    @Override
    @Transactional
    public void save(Theater theater) {
        theaterRepository.save(theater);
    }

    @Override
    @Transactional
    @CacheEvict(value = "theater", key = "#theaterId")
    public void uploadLogo(Long theaterId, MultipartFile file) {
        log.info("Uploading logo for theater: {}", theaterId);

        try {
            String url = imageService.uploadImage(file, "theaters");
            Theater theater = findTheaterById(theaterId);
            theater.setLogoUrl(url);
            theaterRepository.save(theater);

            log.info("Logo uploaded successfully for theater: {}", theaterId);
        } catch (Exception e) {
            log.error("Failed to upload logo for theater: {}", theaterId, e);
            throw new RuntimeException("Failed to upload logo", e);
        }
    }

    // Helper methods
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + username));
    }

    private Theater findTheaterById(Long theaterId) {
        return theaterRepository.findById(theaterId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Theater not found: " + theaterId));
    }

    private Screen findScreenById(Long screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Screen not found: " + screenId));
    }

    private void validateOwnership(Theater theater, User owner) {
        if (theater.getOwner() == null || !theater.getOwner().getId().equals(owner.getId())) {
            throw new CustomExceptions.AccessDeniedException("Access denied: Not the owner of theater");
        }
    }

    private void validateScreenOwnership(Screen screen, User owner) {
        if (screen.getTheater() == null || screen.getTheater().getOwner() == null
                || !screen.getTheater().getOwner().getId().equals(owner.getId())) {
            throw new CustomExceptions.AccessDeniedException("Access denied: Not the owner of screen");
        }
    }

    private Theater buildTheater(TheaterRequest request, User owner) {
        return Theater.builder()
                .name(request.name())
                .location(request.location())
                .amenities(request.amenities())
                .owner(owner)
                .build();
    }

    private Screen buildScreen(ScreenRequest request, Theater theater) {
        return Screen.builder()
                .name(request.name())
                .category(request.category())
                .theater(theater)
                .build();
    }

    private void updateTheaterFields(Theater theater, TheaterRequest request) {
        theater.setName(request.name());
        theater.setLocation(request.location());
        theater.setAmenities(request.amenities());
    }

    private void updateScreenFields(Screen screen, ScreenRequest request) {
        screen.setName(request.name());
        screen.setCategory(request.category());
    }

    private Set<String> parseAmenities(String amenities) {
        if (amenities == null || amenities.isBlank()) {
            return Collections.emptySet();
        }

        return Arrays.stream(amenities.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private Map<String, Integer> generateMonthlyBookingCounts(Long ownerId) {
        YearMonth now = YearMonth.now();
        Map<String, Integer> monthToCount = new LinkedHashMap<>();

        for (int i = ANALYTICS_MONTHS - 1; i >= 0; i--) {
            YearMonth yearMonth = now.minusMonths(i);
            LocalDateTime[] dateRange = getMonthDateRange(yearMonth);

            Long count = bookingRepository.countByOwnerIdAndBookingTimeBetween(
                    ownerId, dateRange[0], dateRange[1]);
            monthToCount.put(yearMonth.toString(), count != null ? count.intValue() : 0);
        }

        return monthToCount;
    }

    private Map<String, Integer> generateMonthlyRevenueCounts(Long ownerId) {
        YearMonth now = YearMonth.now();
        Map<String, Integer> monthToRevenue = new LinkedHashMap<>();

        for (int i = ANALYTICS_MONTHS - 1; i >= 0; i--) {
            YearMonth yearMonth = now.minusMonths(i);
            LocalDateTime[] dateRange = getMonthDateRange(yearMonth);

            BigDecimal revenue = bookingRepository.sumTotalAmountByOwnerIdAndBookingTimeBetween(
                    ownerId, dateRange[0], dateRange[1]);
            monthToRevenue.put(yearMonth.toString(), revenue != null ? revenue.intValue() : 0);
        }

        return monthToRevenue;
    }

    private LocalDateTime[] getMonthDateRange(YearMonth yearMonth) {
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        return new LocalDateTime[] { start, end };
    }

    private Specification<Theater> buildTheaterSpecification(TheaterFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (filter.name() != null && !filter.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase() + "%"));
            }
            if (filter.location() != null && !filter.location().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + filter.location().toLowerCase() + "%"));
            }
            if (filter.amenity() != null && !filter.amenity().isBlank()) {
                predicates.add(cb.isMember(filter.amenity(), root.get("amenities")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}