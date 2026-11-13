package com.thomascook.hotelbooking.web;

import com.thomascook.hotelbooking.domain.User;
import com.thomascook.hotelbooking.dto.*;
import com.thomascook.hotelbooking.exception.AuthenticationFailedException;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.repository.UserRepository;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/account")
public class AccountController {
  private final UserRepository users; private final PasswordEncoder passwords;
  public AccountController(UserRepository users, PasswordEncoder passwords) { this.users=users; this.passwords=passwords; }
  @GetMapping public AuthResponse.UserResponse me() { return view(current()); }
  @PatchMapping public AuthResponse.UserResponse update(@Valid @RequestBody UpdateProfileRequest request) { User user=current(); user.setDisplayName(request.displayName().trim()); return view(users.save(user)); }
  @PostMapping("/password") public void password(@Valid @RequestBody ChangePasswordRequest request) { User user=current(); if (!passwords.matches(request.currentPassword(),user.getPasswordHash())) throw new AuthenticationFailedException(); user.setPasswordHash(passwords.encode(request.newPassword())); users.save(user); }
  private User current() { var a=SecurityContextHolder.getContext().getAuthentication(); if(a==null || !(a.getPrincipal() instanceof String)) throw new InvalidRequestException("Authentication is required"); return users.findByEmailIgnoreCase(((String)a.getPrincipal()).toLowerCase(Locale.ROOT)).orElseThrow(() -> new InvalidRequestException("Account not found")); }
  private AuthResponse.UserResponse view(User u) { return new AuthResponse.UserResponse(u.getId(),u.getEmail(),u.getDisplayName()); }
}
