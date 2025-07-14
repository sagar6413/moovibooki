package com.moviebooking.controller;

import com.moviebooking.model.dto.request.UpdateProfileRequest;
import com.moviebooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and management")
public class UserController {

    private final UserService userService;

    @PostMapping("/profile/image")
    @Operation(summary = "Upload profile image", description = "Upload a profile image for the current user.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> uploadProfileImage(
            @Parameter(description = "Profile image file") @RequestParam("file") MultipartFile file,
            Principal principal) {
        String url = userService.uploadProfileImage(principal.getName(), file);
        return ResponseEntity.ok(url);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update the profile information for the current user.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> updateProfile(
            Principal principal,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Profile update request") @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok().build();
    }
}