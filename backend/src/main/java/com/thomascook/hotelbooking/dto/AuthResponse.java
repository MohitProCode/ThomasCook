package com.thomascook.hotelbooking.dto;
public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {
  public record UserResponse(Long id, String email, String displayName) {}
}
