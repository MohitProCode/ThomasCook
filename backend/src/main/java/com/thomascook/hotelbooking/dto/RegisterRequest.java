package com.thomascook.hotelbooking.dto;
import jakarta.validation.constraints.*;
public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min=8,max=72) String password, @NotBlank @Size(min=2,max=150) String displayName) {}
