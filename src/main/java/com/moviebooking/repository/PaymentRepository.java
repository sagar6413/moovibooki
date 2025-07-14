package com.moviebooking.repository;

import com.moviebooking.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBooking_User_Id(Long userId);

    @Query("SELECT FUNCTION('TO_CHAR', p.paymentTime, 'Mon'), SUM(p.amount) FROM Payment p GROUP BY FUNCTION('TO_CHAR', p.paymentTime, 'Mon') ORDER BY MIN(p.paymentTime)")
    List<Object[]> sumPaymentsByMonth();
}