package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.User;
import com.thomascook.hotelbooking.dto.*;
import com.thomascook.hotelbooking.exception.AuthenticationFailedException;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import com.thomascook.hotelbooking.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service public class AuthService {
  private final UserRepository users; private final PasswordEncoder passwords; private final JwtService jwt;
  public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt) { this.users=users; this.passwords=passwords; this.jwt=jwt; }
  @Transactional public AuthResponse register(RegisterRequest request) {
    String email=normalize(request.email());
    if (users.existsByEmailIgnoreCase(email)) throw new InvalidRequestException("An account with this email already exists");
    try { return response(users.save(new User(email,passwords.encode(request.password()),request.displayName().trim()))); }
    catch (DataIntegrityViolationException ex) { throw new InvalidRequestException("An account with this email already exists"); }
  }
  @Transactional(readOnly=true) public AuthResponse login(LoginRequest request) {
    User user=users.findByEmailIgnoreCase(normalize(request.email())).orElseThrow(AuthenticationFailedException::new);
    if (!passwords.matches(request.password(),user.getPasswordHash())) throw new AuthenticationFailedException();
    return response(user);
  }
  private AuthResponse response(User u) { return new AuthResponse(jwt.issue(u.getEmail()),"Bearer",jwt.expiresInSeconds(),new AuthResponse.UserResponse(u.getId(),u.getEmail(),u.getDisplayName())); }
  public String normalize(String email) { return email.trim().toLowerCase(java.util.Locale.ROOT); }
}
