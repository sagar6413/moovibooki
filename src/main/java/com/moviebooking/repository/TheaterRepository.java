package com.moviebooking.repository;

import com.moviebooking.model.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, Long>, JpaSpecificationExecutor<Theater> {
    List<Theater> findByOwnerId(Long ownerId);

    Page<Theater> findByOwnerId(Long ownerId, Pageable pageable);
}