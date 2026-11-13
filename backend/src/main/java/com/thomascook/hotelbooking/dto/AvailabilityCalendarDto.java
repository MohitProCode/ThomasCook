package com.thomascook.hotelbooking.dto;

import java.time.LocalDate;
import java.util.List;

public record AvailabilityCalendarDto(
    Long hotelId,
    LocalDate from,
    LocalDate to,
    List<AvailabilityDayDto> days) {}
