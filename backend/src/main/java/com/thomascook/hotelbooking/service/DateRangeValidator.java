package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.exception.InvalidDateRangeException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class DateRangeValidator {

  private final Clock clock;

  public DateRangeValidator(Clock clock) {
    this.clock = clock;
  }

  /** Validates one date pair; both must be non-null. */
  public void validate(LocalDate checkIn, LocalDate checkOut) {
    if (checkIn == null || checkOut == null) {
      throw new InvalidDateRangeException("Both checkIn and checkOut dates are required");
    }
    if (!checkOut.isAfter(checkIn)) {
      throw new InvalidDateRangeException("Check-out date must be after check-in date");
    }
    if (checkIn.isBefore(LocalDate.now(clock))) {
      throw new InvalidDateRangeException("Check-in date cannot be in the past");
    }
  }

  /** Validates optional query dates: either both present (then range-checked) or both absent. */
  public void validateOptionalPair(LocalDate checkIn, LocalDate checkOut) {
    if (checkIn == null && checkOut == null) {
      return;
    }
    if (checkIn == null || checkOut == null) {
      throw new InvalidDateRangeException("Both checkIn and checkOut dates are required");
    }
    validate(checkIn, checkOut);
  }
}
