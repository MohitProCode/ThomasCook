package com.thomascook.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Smart alternatives returned when a hotel is sold out for the requested dates. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SmartAlternativesDto(
    NearestAvailableDto nearestAvailable, CheapestAlternativeDto cheapestAlternative) {

  /** Earliest shifted window (same stay length) with at least one free room. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record NearestAvailableDto(
      LocalDate checkIn, LocalDate checkOut, int nights, int availableRooms, String message) {}

  /** Cheapest other hotel in the same location with rooms for the original dates. */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record CheapestAlternativeDto(
      Long hotelId,
      String name,
      String location,
      BigDecimal pricePerNight,
      int availableRooms,
      boolean available,
      LocalDate checkIn,
      LocalDate checkOut,
      String message) {}
}
