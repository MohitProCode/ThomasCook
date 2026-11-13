package com.thomascook.hotelbooking.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HotelContentDto(String hotelId, String name, String description, String address, String city, String country, Double latitude, Double longitude, String mainPhoto, String thumbnail, List<String> amenities, List<String> images, List<RoomContentDto> rooms) {
  public record RoomContentDto(String name, String size, Integer capacity, List<String> amenities, List<String> images) {}
}
