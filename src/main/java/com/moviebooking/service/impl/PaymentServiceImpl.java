package com.moviebooking.service.impl;

import com.moviebooking.exception.CustomExceptions;
import com.moviebooking.model.dto.request.PaymentRequest;
import com.moviebooking.model.dto.response.PaymentResponse;
import com.moviebooking.model.entity.Booking;
import com.moviebooking.model.entity.Payment;
import com.moviebooking.model.enums.PaymentStatus;
import com.moviebooking.repository.BookingRepository;
import com.moviebooking.repository.PaymentRepository;
import com.moviebooking.service.PaymentService;
import com.moviebooking.util.EntityDtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final EntityDtoMapper mapper;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, Long bookingId, Long userId) {
        log.info("Processing payment for booking: {} by user: {}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                                           .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Booking not found"));
        if (!booking.getUser().getId().equals(userId)) {
            throw new CustomExceptions.AccessDeniedException("Not your booking");
        }
        Payment payment = Payment.builder()
                                 .booking(booking)
                                 .amount(booking.getTotalAmount())
                                 .status(PaymentStatus.SUCCESS)
                                 .paymentMethod(request != null ? request.paymentMethod() : null)
                                 .transactionId(request != null ? request.transactionId() : "TXN-" + System.currentTimeMillis())
                                 .paymentTime(LocalDateTime.now())
                                 .build();
        paymentRepository.save(payment);
        booking.setPayment(payment);
        bookingRepository.save(booking);
        log.info("Payment processed: {} for booking: {}", payment.getId(), bookingId);
        return mapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void refundPayment(Long paymentId, Long userId) {
        log.info("Refunding payment: {} by user: {}", paymentId, userId);
        Payment payment = paymentRepository.findById(paymentId)
                                           .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Payment not found"));
        if (!payment.getBooking().getUser().getId().equals(userId)) {
            throw new CustomExceptions.AccessDeniedException("Not your payment");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("Payment refunded: {}", paymentId);
    }

    @Override
    public PaymentResponse getPaymentStatus(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                                           .orElseThrow(() -> new CustomExceptions.UserNotFoundException("Payment not found"));
        return mapper.toPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentHistory(Long userId) {
        List<Payment> payments = paymentRepository.findByBooking_User_Id(userId);
        return payments.stream().map(mapper::toPaymentResponse).collect(Collectors.toList());
    }
}