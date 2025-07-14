package com.moviebooking.controller;

import com.moviebooking.model.dto.request.AddReviewRequest;
import com.moviebooking.model.dto.request.MovieFilter;
import com.moviebooking.model.dto.request.MovieRequest;
import com.moviebooking.model.dto.response.MovieResponse;
import com.moviebooking.model.dto.response.ReviewResponse;
import com.moviebooking.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Movie catalog and management")
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new movie", description = "Admin only. Adds a new movie to the catalog.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Partially update a movie", description = "Admin only. Partially updates movie details.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<MovieResponse> updateMovie(@Parameter(description = "Movie ID") @PathVariable Long movieId,
            @Valid @RequestBody MovieRequest request) {
        MovieResponse response = movieService.updateMovie(movieId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{movieId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a movie", description = "Admin only. Deletes a movie from the catalog.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteMovie(@Parameter(description = "Movie ID") @PathVariable Long movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{movieId}")
    @Operation(summary = "Get movie details", description = "Get details of a specific movie.")
    public ResponseEntity<MovieResponse> getMovie(@Parameter(description = "Movie ID") @PathVariable Long movieId) {
        MovieResponse response = movieService.getMovie(movieId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search movies with pagination", description = "Search and filter movies by title, genre, rating, language, or release date, with pagination.")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @ModelAttribute @Valid MovieFilter filter,
            Pageable pageable) {
        Page<MovieResponse> responses = movieService.searchMovies(filter, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{movieId}/poster")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Upload movie poster", description = "Admin only. Uploads a poster image for a movie.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<String> uploadMoviePoster(@PathVariable Long movieId,
            @Parameter(description = "Poster image file") @RequestParam("file") MultipartFile file) {
        movieService.uploadPoster(movieId, file);
        return ResponseEntity.ok("Poster uploaded successfully");

    }

    @PostMapping("/{movieId}/review")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Add a review to a movie", description = "User or Admin. Adds a review and rating to a movie.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> addReview(@Parameter(description = "Movie ID") @PathVariable Long movieId,
            @RequestBody @Valid AddReviewRequest request,
            Principal principal) {
        movieService.addReview(movieId, principal.getName(), request.review(), request.rating());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{movieId}/reviews")
    @Operation(summary = "Get reviews for a movie", description = "Fetch all reviews for a movie.")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @Parameter(description = "Movie ID") @PathVariable Long movieId,
            Pageable pageable) {
        Page<ReviewResponse> responses = movieService.getReviews(movieId, pageable);
        return ResponseEntity.ok(responses);
    }
}