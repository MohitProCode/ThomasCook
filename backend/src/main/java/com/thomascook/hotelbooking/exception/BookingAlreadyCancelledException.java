package com.thomascook.hotelbooking.exception;

public class BookingAlreadyCancelledException extends RuntimeException {
  public BookingAlreadyCancelledException() {
    super("Booking is already cancelled");
  }
}
