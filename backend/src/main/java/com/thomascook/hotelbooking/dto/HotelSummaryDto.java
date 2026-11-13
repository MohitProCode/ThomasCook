package com.thomascook.hotelbooking.dto;

import java.math.BigDecimal;
import java.util.List;

public record HotelSummaryDto(
    Long id,
    String name,
    String location,
    String description,
    List<String> amenities,
    BigDecimal pricePerNight,
    int totalRooms,
    Integer availableRooms,
    Integer nights,
    BigDecimal totalForStay,
    String source,
    String propertyType,
    String brand,
    String address,
    String postalCode,
    String phone,
    String website,
    Double latitude,
    Double longitude,
    String mainPhoto,
    String thumbnail,
    Double guestRating,
    Double starRating,
    Integer reviewCount,
    String roomName,
    String rateOfferId,
    String boardName) {
  public HotelSummaryDto(Long id, String name, String location, String description,
      List<String> amenities, BigDecimal pricePerNight, int totalRooms,
      Integer availableRooms, Integer nights, BigDecimal totalForStay) {
    this(id, name, location, description, amenities, pricePerNight, totalRooms,
        availableRooms, nights, totalForStay, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null);
  }
}
