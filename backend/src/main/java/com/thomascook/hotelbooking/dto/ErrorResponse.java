package com.thomascook.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

/** Consistent error envelope for every API failure. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldErrorDto> errors,
    SmartAlternativesDto smartAlternatives) {

  public static ErrorResponse of(
      int status, String error, String message, String path, List<FieldErrorDto> errors) {
    return new ErrorResponse(Instant.now(), status, error, message, path, errors, null);
  }
}
