package com.thomascook.hotelbooking.exception;

import com.thomascook.hotelbooking.dto.SmartAlternativesDto;

public class SoldOutException extends RuntimeException {
  private final SmartAlternativesDto smartAlternatives;

  public SoldOutException(String message, SmartAlternativesDto smartAlternatives) {
    super(message);
    this.smartAlternatives = smartAlternatives;
  }

  public SmartAlternativesDto getSmartAlternatives() {
    return smartAlternatives;
  }
}
