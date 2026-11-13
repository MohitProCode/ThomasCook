package com.thomascook.hotelbooking.exception;

/** Generic message by design: never reveals whether the reference or the email was wrong. */
public class BookingNotFoundException extends RuntimeException {
  public BookingNotFoundException() {
    super("Booking not found for given reference and email");
  }
}
