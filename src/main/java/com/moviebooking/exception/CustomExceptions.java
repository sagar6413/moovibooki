package com.moviebooking.exception;

public class CustomExceptions {
    public static class JwtAuthenticationException extends RuntimeException {
        public JwtAuthenticationException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }

    public static class InvalidRequestException extends RuntimeException {
        public InvalidRequestException(String message) {
            super(message);
        }
    }

    public static class ShowNotFoundException extends RuntimeException {
        public ShowNotFoundException(String message) {
            super(message);
        }
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(String message) {
            super(message);
        }
    }

    public static class SeatUnavailableException extends RuntimeException {
        public SeatUnavailableException(String message) {
            super(message);
        }
    }

    public static class SeatLockedException extends RuntimeException {
        public SeatLockedException(String message) {
            super(message);
        }
    }

    public static class ImageUploadException extends RuntimeException {
        public ImageUploadException(String message) {
            super(message);
        }

        public ImageUploadException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class MovieNotFoundException extends RuntimeException {
        public MovieNotFoundException(String message) {
            super(message);
        }
    }
}