package com.thomascook.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thomascook.hotelbooking.domain.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Booking payload. {@code email}/{@code guestEmail} and {@code totalPrice}/{@code totalAmount} are
 * duplicate aliases kept in sync so both documented field names work.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingResponse(
    String reference,
    Long hotelId,
    String hotelName,
    String location,
    String guestName,
    String email,
    String guestEmail,
    LocalDate checkIn,
    LocalDate checkOut,
    int nights,
    BigDecimal pricePerNight,
    BigDecimal totalPrice,
    BigDecimal totalAmount,
    String currency,
    BookingStatus status,
    LocalDateTime createdAt,
    LocalDateTime cancelledAt,
    String roomName,
    String roomOfferId,
    Integer guests) {
  public BookingResponse(String reference, Long hotelId, String hotelName, String location,
      String guestName, String email, String guestEmail, LocalDate checkIn, LocalDate checkOut,
      int nights, BigDecimal pricePerNight, BigDecimal totalPrice, BigDecimal totalAmount,
      String currency, BookingStatus status, LocalDateTime createdAt, LocalDateTime cancelledAt) {
    this(reference, hotelId, hotelName, location, guestName, email, guestEmail, checkIn, checkOut,
        nights, pricePerNight, totalPrice, totalAmount, currency, status, createdAt, cancelledAt,
        null, null, null);
  }
}
