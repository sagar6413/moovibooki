package com.moviebooking.service;

import com.moviebooking.model.dto.request.MovieFilter;
import com.moviebooking.model.dto.request.MovieRequest;
import com.moviebooking.model.dto.response.MovieResponse;
import com.moviebooking.model.dto.response.ReviewResponse;
import com.moviebooking.model.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);

    MovieResponse updateMovie(Long movieId, MovieRequest request);

    void deleteMovie(Long movieId);

    MovieResponse getMovie(Long movieId);

    Page<MovieResponse> searchMovies(MovieFilter filter, Pageable pageable);

    void uploadPoster(Long movieId, MultipartFile file);

    void addReview(Long movieId, String username, String review, int rating);

    Page<ReviewResponse> getReviews(Long movieId, Pageable pageable);

    Movie getMovieById(Long id);

    void save(Movie movie);
}