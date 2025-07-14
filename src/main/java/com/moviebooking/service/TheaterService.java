package com.moviebooking.service;

import com.moviebooking.model.dto.request.ScreenRequest;
import com.moviebooking.model.dto.request.TheaterFilter;
import com.moviebooking.model.dto.request.TheaterRequest;
import com.moviebooking.model.dto.response.AnalyticsData;
import com.moviebooking.model.dto.response.ScreenResponse;
import com.moviebooking.model.dto.response.TheaterResponse;
import com.moviebooking.model.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface TheaterService {
    TheaterResponse registerTheater(TheaterRequest request, String username);

    TheaterResponse updateTheater(Long theaterId, TheaterRequest request, String username);

    void deleteTheater(Long theaterId, String username);

    TheaterResponse getTheater(Long theaterId);

    Page<TheaterResponse> getTheatersByOwner(String username, Pageable pageable);

    ScreenResponse addScreen(Long theaterId, ScreenRequest request, String username);

    ScreenResponse updateScreen(Long screenId, ScreenRequest request, String username);

    void deleteScreen(Long screenId, String username);

    Page<ScreenResponse> getScreensByTheater(Long theaterId, Pageable pageable);

    void updateAmenities(Long theaterId, String amenities, String username);

    Page<TheaterResponse> listTheaters(TheaterFilter filter, Pageable pageable);

    AnalyticsData getOwnerBookingsAnalytics(String username);

    AnalyticsData getOwnerRevenueAnalytics(String username);

    Theater getTheaterById(Long id);

    void save(Theater theater);

    void uploadLogo(Long theaterId, MultipartFile file);
}