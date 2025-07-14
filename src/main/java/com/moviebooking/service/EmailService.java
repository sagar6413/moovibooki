package com.moviebooking.service;

import com.moviebooking.model.entity.User;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    CompletableFuture<Void> sendVerificationEmail(User user, String token);

    CompletableFuture<Void> sendPasswordResetEmail(User user, String token);
}