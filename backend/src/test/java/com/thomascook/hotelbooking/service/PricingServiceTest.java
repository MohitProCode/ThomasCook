package com.thomascook.hotelbooking.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

  private final PricingService pricing = new PricingService();

  @Test
  void nights_times_rate() {
    assertThat(pricing.nightsBetween(LocalDate.parse("2026-12-20"), LocalDate.parse("2026-12-23")))
        .isEqualTo(3);
    assertThat(pricing.totalForStay(new BigDecimal("100.00"), 3))
        .isEqualByComparingTo(new BigDecimal("300.00"));
  }

  @Test
  void single_night() {
    assertThat(pricing.nightsBetween(LocalDate.parse("2026-12-20"), LocalDate.parse("2026-12-21")))
        .isEqualTo(1);
    assertThat(pricing.totalForStay(new BigDecimal("4500.00"), 1))
        .isEqualByComparingTo(new BigDecimal("4500.00"));
  }

  @Test
  void month_boundary_nights() {
    assertThat(pricing.nightsBetween(LocalDate.parse("2026-01-31"), LocalDate.parse("2026-02-02")))
        .isEqualTo(2);
  }

  @Test
  void scale_is_exactly_two_decimals() {
    assertThat(pricing.totalForStay(new BigDecimal("99.995"), 1).scale()).isEqualTo(2);
    assertThat(pricing.totalForStay(new BigDecimal("333.335"), 3))
        .isEqualByComparingTo(new BigDecimal("1000.01"));
  }
}
