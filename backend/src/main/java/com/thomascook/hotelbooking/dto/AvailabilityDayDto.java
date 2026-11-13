package com.thomascook.hotelbooking.dto;

import java.time.LocalDate;

public record AvailabilityDayDto(
    LocalDate date,
    int availableRooms,
    int totalRooms,
    int occupancyPercentage,
    String status) {}
