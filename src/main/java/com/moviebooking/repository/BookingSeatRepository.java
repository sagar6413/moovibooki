package com.moviebooking.repository;

import com.moviebooking.model.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    List<BookingSeat> findByBooking_Show_Id(Long showId);

    List<BookingSeat> findByBookingId(Long bookingId);
}