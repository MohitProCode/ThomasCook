package com.thomascook.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HotelDetailDto(
    Long id,
    String name,
    String location,
    String description,
    List<String> amenities,
    BigDecimal pricePerNight,
    int totalRooms,
    Integer availableRooms,
    AvailabilityDto availability,
    SmartAlternativesDto smartAlternatives,
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
    String boardName,
    List<String> images) {
  public HotelDetailDto(Long id, String name, String location, String description,
      List<String> amenities, BigDecimal pricePerNight, int totalRooms,
      Integer availableRooms, AvailabilityDto availability, SmartAlternativesDto smartAlternatives) {
    this(id, name, location, description, amenities, pricePerNight, totalRooms,
        availableRooms, availability, smartAlternatives, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null);
  }
}
