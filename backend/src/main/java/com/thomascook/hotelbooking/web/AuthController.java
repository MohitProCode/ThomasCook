package com.thomascook.hotelbooking.web;

import com.thomascook.hotelbooking.dto.*;
import com.thomascook.hotelbooking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;
  private final com.thomascook.hotelbooking.service.PasswordResetService resets;
  public AuthController(AuthService auth, com.thomascook.hotelbooking.service.PasswordResetService resets) { this.auth=auth; this.resets=resets; }
  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }
  @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
  @PostMapping("/forgot-password") public void forgot(@Valid @RequestBody ForgotPasswordRequest request) { resets.request(request); }
  @PostMapping("/reset-password") public void reset(@Valid @RequestBody ResetPasswordRequest request) { resets.reset(request); }
}
