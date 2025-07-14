package com.moviebooking.controller;

import com.moviebooking.model.dto.request.*;
import com.moviebooking.model.dto.response.AuthResponse;
import com.moviebooking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "User authentication and registration")
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and sends email verification.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email", description = "Verify user email with code sent to email.")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        userService.verifyEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/initiate")
    @Operation(summary = "Initiate password reset", description = "Send password reset token to email.")
    public ResponseEntity<Void> initiatePasswordReset(
            @Parameter(description = "User email") @RequestParam String email) {
        userService.sendPasswordResetMail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/complete")
    @Operation(summary = "Complete password reset", description = "Reset password using token sent to email.")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        userService.changePassword(request, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidate access and refresh tokens.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(@RequestParam String accessToken) {
        userService.logout(accessToken);
        return ResponseEntity.ok().build();
    }
}