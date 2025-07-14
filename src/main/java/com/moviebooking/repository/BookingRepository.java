package com.moviebooking.repository;

import com.moviebooking.model.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);

    // Find bookings for a theater owner between two dates
    @Query("SELECT b FROM Booking b WHERE b.show.screen.theater.owner.id = :ownerId AND b.bookingTime BETWEEN :start AND :end")
    List<Booking> findByOwnerIdAndBookingTimeBetween(Long ownerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT FUNCTION('TO_CHAR', b.bookingTime, 'Mon'), COUNT(b) FROM Booking b GROUP BY FUNCTION('TO_CHAR', b.bookingTime, 'Mon') ORDER BY MIN(b.bookingTime)")
    List<Object[]> countBookingsByMonth();

    List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);

    Page<Booking> findByUserId(Long userId, Pageable pageable);

    @Query("""
                SELECT COUNT(b) FROM Booking b
                WHERE b.show.screen.theater.owner.id = :ownerId
                AND b.bookingTime BETWEEN :startDate AND :endDate
            """)
    Long countByOwnerIdAndBookingTimeBetween(@Param("ownerId") Long ownerId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
                SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
                WHERE b.show.screen.theater.owner.id = :ownerId
                AND b.bookingTime BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumTotalAmountByOwnerIdAndBookingTimeBetween(@Param("ownerId") Long ownerId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}