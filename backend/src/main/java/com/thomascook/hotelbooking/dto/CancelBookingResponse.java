package com.thomascook.hotelbooking.dto;

import com.thomascook.hotelbooking.domain.BookingStatus;
import java.time.LocalDateTime;

public record CancelBookingResponse(
    String reference, BookingStatus status, LocalDateTime cancelledAt, String message) {}
