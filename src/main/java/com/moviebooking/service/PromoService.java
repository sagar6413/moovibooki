package com.moviebooking.service;

import com.moviebooking.model.entity.Booking;
import com.moviebooking.model.entity.PromoCode;

import java.math.BigDecimal;

public interface PromoService {
    /**
     * Validates the promo code and returns the discount amount to apply.
     * Throws an exception if invalid.
     */
    BigDecimal validatePromoCode(PromoCode promoCode, Booking booking);

    PromoCode getPromoCodeById(Long id);

    BigDecimal calculateDiscount(PromoCode promoCode, Booking booking);
}