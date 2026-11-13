package com.thomascook.hotelbooking.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoomOptionDto(String offerId, String roomName, String roomSize, Integer capacity, String boardName, BigDecimal pricePerNight, BigDecimal totalPrice, String refundableTag, List<String> amenities, String image, int availableRooms) {}
