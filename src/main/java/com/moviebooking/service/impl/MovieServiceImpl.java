package com.moviebooking.service.impl;

import com.moviebooking.exception.CustomExceptions;
import com.moviebooking.model.dto.request.MovieFilter;
import com.moviebooking.model.dto.request.MovieRequest;
import com.moviebooking.model.dto.response.MovieResponse;
import com.moviebooking.model.dto.response.ReviewResponse;
import com.moviebooking.model.entity.Actor;
import com.moviebooking.model.entity.Movie;
import com.moviebooking.model.entity.Review;
import com.moviebooking.model.entity.User;
import com.moviebooking.model.enums.Genre;
import com.moviebooking.model.enums.Language;
import com.moviebooking.repository.ActorRepository;
import com.moviebooking.repository.MovieRepository;
import com.moviebooking.repository.ReviewRepository;
import com.moviebooking.repository.UserRepository;
import com.moviebooking.service.ImageService;
import com.moviebooking.service.MovieService;
import com.moviebooking.util.EntityDtoMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ActorRepository actorRepository;
    private final ImageService imageService;
    private final EntityDtoMapper mapper;

    @Override
    @Transactional
    @CachePut(value = "movie", key = "#result.movieId")
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse createMovie(MovieRequest request) {
        log.info("Creating movie: {}", request.title());

        validateMovieRequest(request);

        Movie movie = buildMovieFromRequest(request);
        Movie savedMovie = movieRepository.save(movie);

        log.info("Movie created with ID: {}", savedMovie.getId());
        return toResponse(savedMovie, List.of());
    }

    @Override
    @Transactional
    @CachePut(value = "movie", key = "#movieId")
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse updateMovie(Long movieId, MovieRequest request) {
        log.info("Updating movie with ID: {}", movieId);

        validateMovieRequest(request);

        Movie movie = findMovieById(movieId);
        updateMovieFromRequest(movie, request);
        Movie savedMovie = movieRepository.save(movie);

        log.info("Movie updated with ID: {}", movieId);
        return toResponse(savedMovie, List.of());
    }

    @Override
    @Transactional
    @CacheEvict(value = { "movie", "movies" }, allEntries = true)
    public void deleteMovie(Long movieId) {
        log.info("Deleting movie with ID: {}", movieId);

        if (!movieRepository.existsById(movieId)) {
            throw new CustomExceptions.MovieNotFoundException("Movie not found with ID: " + movieId);
        }

        movieRepository.deleteById(movieId);
        log.info("Movie deleted with ID: {}", movieId);
    }

    @Override
    @Cacheable(value = "movie", key = "#movieId")
    public MovieResponse getMovie(Long movieId) {
        Movie movie = findMovieById(movieId);
        List<ReviewResponse> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId)
                .stream()
                .map(this::toReviewResponse)
                .collect(Collectors.toList());
        return toResponse(movie, reviews);
    }

    @Override
    @Cacheable(value = "movieReviews", key = "#movieId")
    public Page<ReviewResponse> getReviews(Long movieId, Pageable pageable) {
        if (!movieRepository.existsById(movieId)) {
            throw new CustomExceptions.UserNotFoundException("Movie not found with ID: " + movieId);
        }

        Page<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable);
        return reviews.map(this::toReviewResponse);
    }

    @Override
    public Page<MovieResponse> searchMovies(MovieFilter filter, Pageable pageable) {
        Specification<Movie> spec = buildMovieSpecification(filter);
        Page<Movie> movies = movieRepository.findAll(spec, pageable);
        return movies.map(movie -> toResponse(movie, List.of()));
    }

    @Override
    @Transactional
    @CacheEvict(value = "movie", key = "#movieId")
    public void uploadPoster(Long movieId, MultipartFile file) {
        log.info("Uploading poster for movie with ID: {}", movieId);

        validateImageFile(file);

        try {
            String url = imageService.uploadImage(file, "movies");
            Movie movie = findMovieById(movieId);
            movie.setPosterUrl(url);
            movieRepository.save(movie);

            log.info("Poster uploaded successfully for movie with ID: {}", movieId);
        } catch (Exception e) {
            log.error("Failed to upload poster for movie with ID: {}", movieId, e);
            throw new RuntimeException("Failed to upload poster: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = { "movie", "movieReviews" }, key = "#movieId")
    public void addReview(Long movieId, String username, String review, int rating) {
        log.info("Adding review for movie with ID: {} by user: {}", movieId, username);

        validateReviewParams(review, rating);

        Movie movie = findMovieById(movieId);
        User user = findUserByUsername(username);

        // Check if the user already reviewed this movie
        boolean hasReviewed = reviewRepository.existsByMovieIdAndUserId(movieId, user.getId());
        if (hasReviewed) {
            throw new IllegalStateException("User has already reviewed this movie");
        }

        Review reviewEntity = Review.builder()
                .movie(movie)
                .user(user)
                .review(review)
                .rating(rating)
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(reviewEntity);
        log.info("Review added for movie with ID: {} by user: {}", movieId, username);
    }

    @Override
    public Movie getMovieById(Long id) {
        return findMovieById(id);
    }

    @Override
    @Transactional
    public void save(Movie movie) {
        movieRepository.save(movie);
    }

    // Helper methods

    private void validateMovieRequest(MovieRequest request) {
        if (!StringUtils.hasText(request.title())) {
            throw new IllegalArgumentException("Movie title cannot be empty");
        }
        if (request.duration() <= 0) {
            throw new IllegalArgumentException("Movie duration must be positive");
        }
        if (request.releaseDate() != null && request.releaseDate().isAfter(LocalDate.now().plusYears(2))) {
            throw new IllegalArgumentException("Release date cannot be more than 2 years in the future");
        }
    }

    private void validatePaginationParams(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number must be greater than 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    private void validateReviewParams(String review, int rating) {
        if (!StringUtils.hasText(review)) {
            throw new IllegalArgumentException("Review text cannot be empty");
        }
        if (rating < 1 || rating > 10) {
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }
    }

    private Movie buildMovieFromRequest(MovieRequest request) {
        return Movie.builder()
                .title(request.title())
                .genre(request.genre())
                .duration(request.duration())
                .rating(request.rating())
                .cast(resolveActors(request.cast()))
                .synopsis(request.synopsis())
                .releaseDate(request.releaseDate())
                .language(request.language())
                .build();
    }

    private void updateMovieFromRequest(Movie movie, MovieRequest request) {
        movie.setTitle(request.title());
        movie.setGenre(request.genre());
        movie.setDuration(request.duration());
        movie.setRating(request.rating());
        movie.setCast(resolveActors(request.cast()));
        movie.setSynopsis(request.synopsis());
        movie.setReleaseDate(request.releaseDate());
        movie.setLanguage(request.language());
    }

    private Movie findMovieById(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new CustomExceptions.MovieNotFoundException("Movie not found with ID: " + movieId));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found: " + username));
    }

    private Set<Actor> resolveActors(Set<Long> actorIds) {
        if (actorIds == null || actorIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Actor> actors = actorRepository.findAllById(actorIds);
        if (actors.size() != actorIds.size()) {
            Set<Long> foundIds = actors.stream().map(Actor::getId).collect(Collectors.toSet());
            Set<Long> missingIds = actorIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new RuntimeException("Actors not found with IDs: " + missingIds);
        }

        return new HashSet<>(actors);
    }

    private Specification<Movie> buildMovieSpecification(MovieFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + filter.title().toLowerCase() + "%"));
            }
            if (filter.genre() != null) {
                predicates.add(cb.equal(root.get("genre"), filter.genre()));
            }
            if (filter.rating() != null && !filter.rating().isBlank()) {
                predicates.add(cb.equal(root.get("rating"), filter.rating()));
            }
            if (filter.language() != null) {
                predicates.add(cb.equal(root.get("language"), filter.language()));
            }
            if (filter.releaseDate() != null && !filter.releaseDate().isBlank()) {
                predicates.add(cb.equal(root.get("releaseDate"), filter.releaseDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Genre parseGenre(String genre) {
        try {
            return Genre.valueOf(genre.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid genre: {}", genre);
            return null;
        }
    }

    private Language parseLanguage(String language) {
        try {
            return Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid language: {}", language);
            return null;
        }
    }

    private MovieResponse toResponse(Movie movie, List<ReviewResponse> reviews) {
        if (movie == null) {
            throw new CustomExceptions.MovieNotFoundException("Movie not found");
        }
        return mapper.toMovieResponse(movie, reviews);
    }

    private ReviewResponse toReviewResponse(Review review) {
        return mapper.toReviewResponse(
                review.getUser().getUsername(),
                review.getReview(),
                review.getRating(),
                review.getCreatedAt());
    }
}