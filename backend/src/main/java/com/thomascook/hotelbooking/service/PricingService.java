package com.thomascook.hotelbooking.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

  /** Nights in the half-open range [checkIn, checkOut). */
  public int nightsBetween(LocalDate checkIn, LocalDate checkOut) {
    return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
  }

  /** Server-side total: nights x price/night, always 2dp HALF_UP. Never trusts client totals. */
  public BigDecimal totalForStay(BigDecimal pricePerNight, int nights) {
    return pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
  }
}
