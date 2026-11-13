package com.thomascook.hotelbooking.dto;

import java.math.BigDecimal;

public record QuoteResponse(
    Long hotelId,
    BigDecimal pricePerNight,
    int nights,
    BigDecimal totalPrice,
    String currency,
    int availableRooms) {}
