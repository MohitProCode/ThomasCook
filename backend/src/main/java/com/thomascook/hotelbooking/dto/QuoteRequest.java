package com.thomascook.hotelbooking.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class QuoteRequest {
  @NotNull(message = "Check-in date is required")
  private LocalDate checkIn;

  @NotNull(message = "Check-out date is required")
  private LocalDate checkOut;

  public LocalDate getCheckIn() {
    return checkIn;
  }

  public void setCheckIn(LocalDate checkIn) {
    this.checkIn = checkIn;
  }

  public LocalDate getCheckOut() {
    return checkOut;
  }

  public void setCheckOut(LocalDate checkOut) {
    this.checkOut = checkOut;
  }
}
