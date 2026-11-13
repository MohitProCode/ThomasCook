package com.thomascook.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateBookingRequest {
  @NotNull(message = "Hotel id is required")
  private Long hotelId;

  @NotBlank(message = "Guest name is required")
  @Size(min = 2, max = 150, message = "Guest name must be between 2 and 150 characters")
  private String guestName;

  @NotBlank(message = "Email is required")
  @Email(message = "Enter a valid email address")
  @JsonAlias("guestEmail")
  private String email;

  @NotNull(message = "Check-in date is required")
  private LocalDate checkIn;

  @NotNull(message = "Check-out date is required")
  private LocalDate checkOut;

  private String roomOfferId;
  private String roomName;
  private Integer guests;

  public Long getHotelId() {
    return hotelId;
  }

  public void setHotelId(Long hotelId) {
    this.hotelId = hotelId;
  }

  public String getGuestName() {
    return guestName;
  }

  public void setGuestName(String guestName) {
    this.guestName = guestName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDate getCheckIn() {
    return checkIn;
  }

  public void setCheckIn(LocalDate checkIn) {
    this.checkIn = checkIn;
  }

  public LocalDate getCheckOut() {
    return checkOut;
  }

  public void setCheckOut(LocalDate checkOut) {
    this.checkOut = checkOut;
  }
  public String getRoomOfferId() { return roomOfferId; }
  public void setRoomOfferId(String roomOfferId) { this.roomOfferId = roomOfferId; }
  public String getRoomName() { return roomName; }
  public void setRoomName(String roomName) { this.roomName = roomName; }
  public Integer getGuests() { return guests; }
  public void setGuests(Integer guests) { this.guests = guests; }
}
