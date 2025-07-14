package com.moviebooking.service;

import com.moviebooking.model.dto.request.*;
import com.moviebooking.model.dto.response.AuthResponse;
import com.moviebooking.model.dto.response.BookingHistoryResponse;
import com.moviebooking.model.dto.response.UserProfileResponse;
import com.moviebooking.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void sendEmailVerificationMail(String email);

    void verifyEmail(EmailVerificationRequest request);

    void sendPasswordResetMail(String email);

    void resetPassword(PasswordResetRequest request);

    UserProfileResponse getProfile(Long userId);

    BookingHistoryResponse getBookingHistory(Long userId);

    void updateProfile(String username, UpdateProfileRequest request);

    User findByUsername(String username);

    void save(User user);

    void logout(String accessToken);

    boolean isTokenBlacklisted(String token);

    void changePassword(ChangePasswordRequest request, String name);

    String uploadProfileImage(String username, MultipartFile file);
}