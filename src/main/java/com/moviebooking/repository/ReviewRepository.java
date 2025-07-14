package com.moviebooking.repository;

import com.moviebooking.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieId(Long movieId);

    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    boolean existsByMovieIdAndUserId(Long movieId, Long id);

    Page<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId, Pageable pageable);
}