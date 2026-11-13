package com.thomascook.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class CancelBookingRequest {
  @JsonAlias("guestEmail")
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
