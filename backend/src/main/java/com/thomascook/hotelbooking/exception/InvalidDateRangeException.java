package com.thomascook.hotelbooking.exception;

public class InvalidDateRangeException extends RuntimeException {
  public InvalidDateRangeException(String message) {
    super(message);
  }
}
