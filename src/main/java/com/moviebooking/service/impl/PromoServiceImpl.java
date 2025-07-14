package com.moviebooking.service.impl;

import com.moviebooking.model.entity.Booking;
import com.moviebooking.model.entity.PromoCode;
import com.moviebooking.repository.PromoCodeRepository;
import com.moviebooking.service.PromoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class PromoServiceImpl implements PromoService {
    private final PromoCodeRepository promoCodeRepository;

    @Override
    public BigDecimal validatePromoCode(PromoCode promoCode, Booking booking) {
        if (promoCode == null || !Boolean.TRUE.equals(promoCode.getIsActive())) {
            throw new IllegalArgumentException("Invalid or inactive promo code");
        }
        // Prefer discountAmount if it's set, otherwise use discountPercentage
        if (promoCode.getDiscountAmount() != null) {
            return promoCode.getDiscountAmount();
        } else if (promoCode.getDiscountPercentage() != null) {
            return booking.getTotalAmount().multiply(promoCode.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public PromoCode getPromoCodeById(Long id) {
        return promoCodeRepository.findById(id).orElse(null);
    }

    @Override
    public BigDecimal calculateDiscount(PromoCode promoCode, Booking booking) {
        if (promoCode == null || !Boolean.TRUE.equals(promoCode.getIsActive())) {
            return BigDecimal.ZERO;
        }
        if (promoCode.getDiscountAmount() != null) {
            return promoCode.getDiscountAmount();
        } else if (promoCode.getDiscountPercentage() != null) {
            return booking.getTotalAmount().multiply(promoCode.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}