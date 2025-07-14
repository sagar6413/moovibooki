package com.moviebooking.model.enums;


/**
 * Enum representing different types of tokens in the system.
 */
public enum TokenType {
    /**
     * Used for email verification upon registration
     */
    EMAIL_VERIFICATION,

    /**
     * Used for password reset functionality
     */
    PASSWORD_RESET,

    /**
     * Used for access tokens (JWT)
     */
    ACCESS,

    /**
     * Used for refresh tokens
     */
    REFRESH
}