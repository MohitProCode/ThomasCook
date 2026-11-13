package com.thomascook.hotelbooking.web;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.thomascook.hotelbooking.dto.ErrorResponse;
import com.thomascook.hotelbooking.dto.FieldErrorDto;
import com.thomascook.hotelbooking.exception.BookingAlreadyCancelledException;
import com.thomascook.hotelbooking.exception.AuthenticationFailedException;
import com.thomascook.hotelbooking.exception.BookingNotFoundException;
import com.thomascook.hotelbooking.exception.HotelNotFoundException;
import com.thomascook.hotelbooking.exception.InvalidDateRangeException;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.exception.SoldOutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Maps every failure to the consistent {timestamp,status,error,message,path,errors} envelope. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponse> handleUploadTooLarge(
      MaxUploadSizeExceededException ex, HttpServletRequest request) {
    return envelope(HttpStatus.PAYLOAD_TOO_LARGE,
        "Inventory file is too large. Maximum allowed size is 200MB.",
        request, List.of(), null);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleBeanValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<FieldErrorDto> errors = new ArrayList<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(fe -> errors.add(new FieldErrorDto(fe.getField(), messageOf(fe.getDefaultMessage()))));
    ex.getBindingResult()
        .getGlobalErrors()
        .forEach(fe -> errors.add(new FieldErrorDto(fe.getObjectName(), messageOf(fe.getDefaultMessage()))));
    return envelope(HttpStatus.BAD_REQUEST, "Validation failed", request, errors, null);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    List<FieldErrorDto> errors =
        ex.getConstraintViolations().stream()
            .map(v -> new FieldErrorDto(v.getPropertyPath().toString(), messageOf(v.getMessage())))
            .toList();
    return envelope(HttpStatus.BAD_REQUEST, "Validation failed", request, errors, null);
  }

  @ExceptionHandler({InvalidDateRangeException.class, InvalidRequestException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(
      RuntimeException ex, HttpServletRequest request) {
    return envelope(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of(), null);
  }

  @ExceptionHandler(AuthenticationFailedException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationFailedException ex, HttpServletRequest request) {
    return envelope(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, List.of(), null);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return envelope(HttpStatus.BAD_REQUEST, dateMessage(ex.getName()), request, List.of(), null);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    if (isDateParseFailure(ex)) {
      return envelope(HttpStatus.BAD_REQUEST, "Use YYYY-MM-DD for dates", request, List.of(), null);
    }
    return envelope(HttpStatus.BAD_REQUEST, "Malformed request body", request, List.of(), null);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    List<FieldErrorDto> errors =
        List.of(new FieldErrorDto(ex.getParameterName(), "Required parameter is missing"));
    return envelope(
        HttpStatus.BAD_REQUEST, "Missing required parameter: " + ex.getParameterName(), request,
        errors, null);
  }

  @ExceptionHandler({HotelNotFoundException.class, BookingNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, HttpServletRequest request) {
    return envelope(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of(), null);
  }

  @ExceptionHandler(SoldOutException.class)
  public ResponseEntity<ErrorResponse> handleSoldOut(SoldOutException ex, HttpServletRequest request) {
    return envelope(
        HttpStatus.CONFLICT, ex.getMessage(), request, List.of(), ex.getSmartAlternatives());
  }

  @ExceptionHandler(BookingAlreadyCancelledException.class)
  public ResponseEntity<ErrorResponse> handleAlreadyCancelled(
      BookingAlreadyCancelledException ex, HttpServletRequest request) {
    return envelope(HttpStatus.CONFLICT, ex.getMessage(), request, List.of(), null);
  }

  @ExceptionHandler(PessimisticLockException.class)
  public ResponseEntity<ErrorResponse> handleLockTimeout(
      PessimisticLockException ex, HttpServletRequest request) {
    return envelope(
        HttpStatus.CONFLICT, "High demand \u2014 please retry", request, List.of(), null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    return envelope(
        HttpStatus.CONFLICT, "Request conflicts with existing data", request, List.of(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled API error for {}", request.getRequestURI(), ex);
    return envelope(
        HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request, List.of(), null);
  }

  private ResponseEntity<ErrorResponse> envelope(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      List<FieldErrorDto> errors,
      com.thomascook.hotelbooking.dto.SmartAlternativesDto alternatives) {
    ErrorResponse body =
        new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            errors,
            alternatives);
    return ResponseEntity.status(status).body(body);
  }

  private String dateMessage(String paramName) {
    if ("checkIn".equals(paramName) || "checkOut".equals(paramName)) {
      return "Use YYYY-MM-DD for dates";
    }
    return "Invalid value for parameter: " + paramName;
  }

  private boolean isDateParseFailure(HttpMessageNotReadableException ex) {
    Throwable cause = ex.getCause();
    while (cause != null) {
      if (cause instanceof DateTimeParseException || cause instanceof InvalidFormatException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  private String messageOf(String message) {
    return message == null ? "Invalid value" : message;
  }
}
