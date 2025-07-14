package com.moviebooking.service.impl;

import com.moviebooking.exception.CustomExceptions;
import com.moviebooking.model.dto.request.*;
import com.moviebooking.model.dto.response.AuthResponse;
import com.moviebooking.model.dto.response.BookingHistoryResponse;
import com.moviebooking.model.dto.response.BookingSummaryDto;
import com.moviebooking.model.dto.response.UserProfileResponse;
import com.moviebooking.model.entity.Booking;
import com.moviebooking.model.entity.Role;
import com.moviebooking.model.entity.User;
import com.moviebooking.model.entity.VerificationToken;
import com.moviebooking.model.enums.TokenType;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.RoleRepository;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.repository.VerificationTokenRepository;
import com.moviebooking.service.EmailService;
import com.moviebooking.service.ImageService;
import com.moviebooking.service.UserService;
import com.moviebooking.util.EntityDtoMapper;
import com.moviebooking.util.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    // Common constants
    private static final String EMAIL_VERIFICATION_TOKENS_CACHE = "emailVerificationTokens";
    private static final String PASSWORD_RESET_TOKENS_CACHE = "passwordResetTokens";
    private static final String REFRESH_TOKENS_CACHE = "refreshTokens";
    private static final String BLACKLISTED_TOKENS_CACHE = "blacklistedTokens";
    private static final String USER_PROFILE_CACHE = "userProfile";
    private static final String USER_BOOKING_HISTORY_CACHE = "userBookingHistory";
    private static final String ROLES_CACHE = "roles";

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 7;
    private static final int TOKEN_RATE_LIMIT_MINUTES = 1;

    // Dependencies for this service
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BookingRepository bookingRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedissonClient redissonClient;
    private final EmailService emailService;
    private final EntityDtoMapper mapper;
    private final ImageService imageService;

    // Config properties
    @Value("${app.token.verification.duration-minutes:60}")
    private int verificationTokenExpiryMinutes;

    @Value("${app.token.reset-password.duration-minutes:30}")
    private int resetPasswordTokenExpiryMinutes;

    // Redis cache helpers
    private volatile RMapCache<String, String> emailVerificationTokensCache;
    private volatile RMapCache<String, String> passwordResetTokensCache;
    private volatile RMapCache<String, String> refreshTokensCache;
    private volatile RSetCache<String> blacklistedTokensCache;

    @PostConstruct
    private void initCaches() {
        emailVerificationTokensCache = redissonClient.getMapCache(EMAIL_VERIFICATION_TOKENS_CACHE);
        passwordResetTokensCache = redissonClient.getMapCache(PASSWORD_RESET_TOKENS_CACHE);
        refreshTokensCache = redissonClient.getMapCache(REFRESH_TOKENS_CACHE);
        blacklistedTokensCache = redissonClient.getSetCache(BLACKLISTED_TOKENS_CACHE);
    }

    private RMapCache<String, String> emailVerificationTokens() {
        return emailVerificationTokensCache;
    }

    private RMapCache<String, String> passwordResetTokens() {
        return passwordResetTokensCache;
    }

    private RMapCache<String, String> refreshTokens() {
        return refreshTokensCache;
    }

    private RSetCache<String> blacklistedTokens() {
        return blacklistedTokensCache;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_PROFILE_CACHE, allEntries = true),
            @CacheEvict(value = USER_BOOKING_HISTORY_CACHE, allEntries = true)
    })
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        // Check input values
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.username()) ||
                !StringUtils.hasText(request.password())) {
            throw new CustomExceptions.JwtAuthenticationException("Missing required fields");
        }

        // Check if user exists
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomExceptions.JwtAuthenticationException("Email already registered");
        }

        // Find role or throw error
        Role role = findRoleByName(request.role().name());

        // Create and save user
        User user = createUser(request, role);
        User savedUser = userRepository.save(user);

        // Generate token and send email
        String verificationToken = generateVerificationToken(savedUser, TokenType.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(savedUser, verificationToken);

        log.info("User registered successfully: {}. Verification email sent.", savedUser.getEmail());

        return buildAuthResponse(savedUser, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.email());

        // Check input values
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new CustomExceptions.JwtAuthenticationException("Missing required fields");
        }

        // Authenticate
        Authentication authentication = authenticateUser(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Find user and check
        User user = findUserByEmailWithRoles(request.email());
        validateUserForLogin(user);

        // Generate tokens
        List<SimpleGrantedAuthority> authorities = extractAuthorities(user);
        String accessToken = jwtUtil.generateToken(user.getEmail(), authorities);
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Store refresh token in cache
        refreshTokens().put(user.getEmail(), refreshToken, REFRESH_TOKEN_VALIDITY_DAYS, TimeUnit.DAYS);

        log.info("User logged in successfully: {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public void sendEmailVerificationMail(String email) {
        log.info("Sending email verification mail to: {}", email);

        User user = findUserByEmail(email);

        if (user.isEmailVerified()) {
            throw new CustomExceptions.JwtAuthenticationException("Email already verified");
        }

        // Check rate limit
        checkTokenRateLimit(user, TokenType.EMAIL_VERIFICATION);

        // Remove old token and make new one
        deleteExistingToken(user, TokenType.EMAIL_VERIFICATION);
        String newToken = generateVerificationToken(user, TokenType.EMAIL_VERIFICATION);
        emailService.sendVerificationEmail(user, newToken);

        log.info("Verification email sent successfully for user: {}", email);
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE, key = "#request.email")
    public void verifyEmail(EmailVerificationRequest request) {
        log.info("Verifying email for: {}", request.email());

        // Find and check verification token
        VerificationToken verificationToken = findVerificationToken(request.verificationCode());
        validateVerificationToken(verificationToken, TokenType.EMAIL_VERIFICATION);

        // Find user and check ownership
        User userFromDb = findUserByEmail(request.email());
        validateTokenOwnership(verificationToken.getUser(), userFromDb);

        // Update user and token together
        updateUserForEmailVerification(userFromDb);
        confirmToken(verificationToken);

        // Remove token from cache
        emailVerificationTokens().remove(userFromDb.getEmail());

        log.info("Email verified successfully for user: {}", request.email());
    }

    @Override
    @Transactional(readOnly = true)
    public void sendPasswordResetMail(String email) {
        log.info("Initiating password reset for: {}", email);

        User user = findUserByEmail(email);

        // Check rate limit
        checkTokenRateLimit(user, TokenType.PASSWORD_RESET);

        // Remove old reset token and make new one
        deleteExistingToken(user, TokenType.PASSWORD_RESET);
        String resetToken = generateVerificationToken(user, TokenType.PASSWORD_RESET);
        emailService.sendPasswordResetEmail(user, resetToken);

        log.info("Password reset initiated successfully for user: {}", email);
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE, key = "#request.email")
    public void resetPassword(PasswordResetRequest request) {
        log.info("Resetting password for: {}", request.email());

        // Find and check reset token
        VerificationToken verificationToken = findVerificationToken(request.resetToken());
        validateVerificationToken(verificationToken, TokenType.PASSWORD_RESET);

        // Find user and update password
        User user = findUserByEmail(request.email());
        validateTokenOwnership(verificationToken.getUser(), user);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Confirm token and clean up
        confirmToken(verificationToken);
        passwordResetTokens().remove(user.getEmail());

        // Invalidate all refresh tokens
        refreshTokens().remove(user.getEmail());

        log.info("Password reset successfully for user: {}", request.email());
    }

    @Override
    public void changePassword(ChangePasswordRequest request, String name) {
        User user = findUserByUsername(name);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new CustomExceptions.JwtAuthenticationException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", name);
    }

    @Override
    @Cacheable(value = USER_PROFILE_CACHE, key = "#userId")
    public UserProfileResponse getProfile(Long userId) {
        log.debug("Retrieving profile for user: {}", userId);

        User user = findUserByIdWithRoles(userId);
        List<BookingSummaryDto> bookingHistory = getBookingSummariesForUser(userId);

        return mapper.toUserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                extractRoleNames(user),
                user.isEnabled(),
                user.isEmailVerified(),
                bookingHistory);
    }

    @Override
    @Cacheable(value = USER_BOOKING_HISTORY_CACHE, key = "#userId")
    public BookingHistoryResponse getBookingHistory(Long userId) {
        log.debug("Retrieving booking history for user: {}", userId);

        User user = findUserById(userId);
        List<BookingSummaryDto> bookingSummaries = getBookingSummariesForUser(userId);

        return mapper.toBookingHistoryResponse(user.getId(), bookingSummaries);
    }

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE, key = "#username")
    public void updateProfile(String username, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", username);

        User user = findUserByUsername(username);
        boolean isUpdated = false;

        // Update username if given
        if (StringUtils.hasText(request.username()) && !request.username().equals(user.getUsername())) {
            user.setUsername(request.username());
            isUpdated = true;
        }

        // Update email if given and not used
        if (StringUtils.hasText(request.email()) && !request.email().equals(user.getEmail())) {
            validateEmailUpdate(user, request.email());
            user.setEmail(request.email());
            user.setEmailVerified(false); // Needs re-verification
            isUpdated = true;
        }

        if (isUpdated) {
            userRepository.save(user);
            log.info("Profile updated successfully for user: {}", username);
        } else {
            log.debug("No changes detected for user profile: {}", username);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return findUserByUsername(username);
    }

    @Override
    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public void logout(String accessToken) {
        log.info("Logging out user");

        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new RuntimeException("Not authenticated");
            }

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            // Get username before blacklisting
            String username = user.getUsername();

            // Blacklist access token
            Date expiry = jwtUtil.extractExpiration(accessToken);
            long ttl = expiry.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                blacklistedTokens().add(accessToken, ttl, TimeUnit.MILLISECONDS);
            }

            // Invalidate refresh token
            refreshTokens().remove(user.getEmail());
            SecurityContextHolder.clearContext();

            log.info("User logged out successfully: {}", username);
        } catch (Exception e) {
            log.warn("Error during logout process: {}", e.getMessage());
            // Don't throw error to avoid blocking logout
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens().contains(token);
    }

    @Override
    public String uploadProfileImage(String username, MultipartFile file) {
        String url = imageService.uploadImage(file, "users");
        User user = findByUsername(username);
        user.setProfileImageUrl(url);
        save(user);
        return url;
    }

    // Private helpers

    private void validateRegisterRequest(RegisterRequest request) {
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.username()) ||
                !StringUtils.hasText(request.password())) {
            throw new CustomExceptions.JwtAuthenticationException("Missing required fields");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new CustomExceptions.JwtAuthenticationException("Missing required fields");
        }
    }

    @Cacheable(value = ROLES_CACHE, key = "#roleName")
    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Role not found: " + roleName));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with email: " + email));
    }

    private User findUserByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with email: " + email));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with id: " + userId));
    }

    private User findUserByIdWithRoles(Long userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found with id: " + userId));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(
                        () -> new CustomExceptions.UserNotFoundException("User not found with username: " + username));
    }

    private VerificationToken findVerificationToken(String token) {
        return tokenRepository.findByTokenWithUser(token)
                .orElseThrow(() -> new CustomExceptions.JwtAuthenticationException("Invalid verification token"));
    }

    private User createUser(RegisterRequest request, Role role) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(false)
                .emailVerified(false)
                .roles(Set.of(role))
                .build();
    }

    private Authentication authenticateUser(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    }

    private void validateUserForLogin(User user) {
        if (!user.isEnabled() || !user.isEmailVerified()) {
            throw new CustomExceptions.JwtAuthenticationException("Email not verified or user not enabled");
        }
    }

    private List<SimpleGrantedAuthority> extractAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    private Set<String> extractRoleNames(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                extractRoleNames(user));
    }

    private void validateVerificationToken(VerificationToken token, TokenType expectedType) {
        if (token.isExpired()) {
            throw new CustomExceptions.JwtAuthenticationException("Token has expired");
        }

        if (token.isConfirmed()) {
            throw new CustomExceptions.JwtAuthenticationException("Token has already been used");
        }

        if (!token.getTokenType().equals(expectedType)) {
            throw new CustomExceptions.JwtAuthenticationException("Invalid token type");
        }
    }

    private void validateTokenOwnership(User tokenUser, User requestUser) {
        if (!Objects.equals(tokenUser.getId(), requestUser.getId())) {
            throw new CustomExceptions.JwtAuthenticationException("Token does not belong to user");
        }
    }

    private void updateUserForEmailVerification(User user) {
        user.setEmailVerified(true);
        user.setEnabled(true);
        userRepository.save(user);
    }

    private void confirmToken(VerificationToken token) {
        token.setConfirmedAt(LocalDateTime.now());
        tokenRepository.save(token);
    }

    private void deleteExistingToken(User user, TokenType tokenType) {
        tokenRepository.deleteByUserAndTokenType(user, tokenType);
    }

    private void validateEmailUpdate(User user, String newEmail) {
        if (userRepository.existsByEmailAndIdNot(newEmail, user.getId())) {
            throw new CustomExceptions.JwtAuthenticationException("Email already in use");
        }
    }

    private List<BookingSummaryDto> getBookingSummariesForUser(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByBookingTimeDesc(userId);
        return bookings.stream()
                .map(booking -> mapper.toBookingSummaryDto(
                        booking.getId(),
                        booking.getStatus(),
                        booking.getTotalAmount(),
                        booking.getBookingTime() != null ? booking.getBookingTime().toString() : null,
                        Collections.emptyList() // Seat details loaded separately if needed
                ))
                .collect(Collectors.toList());
    }

    private String generateVerificationToken(User user, TokenType tokenType) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryTime = calculateExpiryTime(tokenType);

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .tokenType(tokenType)
                .expiryDate(expiryTime)
                .build();

        tokenRepository.save(verificationToken);
        return token;
    }

    private LocalDateTime calculateExpiryTime(TokenType tokenType) {
        int expiryMinutes = (tokenType == TokenType.PASSWORD_RESET)
                ? resetPasswordTokenExpiryMinutes
                : verificationTokenExpiryMinutes;
        return LocalDateTime.now().plusMinutes(expiryMinutes);
    }

    private void checkTokenRateLimit(User user, TokenType tokenType) {
        Optional<VerificationToken> lastTokenOpt = tokenRepository.findByUserAndTokenType(user, tokenType);
        if (lastTokenOpt.isPresent()) {
            VerificationToken lastToken = lastTokenOpt.get();
            if (lastToken.getCreatedAt() != null &&
                    ChronoUnit.MINUTES.between(lastToken.getCreatedAt(),
                            LocalDateTime.now()) < TOKEN_RATE_LIMIT_MINUTES) {
                throw new CustomExceptions.JwtAuthenticationException(
                        "Please wait before requesting another " + tokenType.name().toLowerCase() + " token.");
            }
        }
    }

}