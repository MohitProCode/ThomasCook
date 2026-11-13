package com.thomascook.hotelbooking.dto;

import java.time.LocalDate;

public record AvailabilityDto(LocalDate checkIn, LocalDate checkOut, int nights, int availableRooms) {}
