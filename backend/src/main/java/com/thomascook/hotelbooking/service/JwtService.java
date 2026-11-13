package com.thomascook.hotelbooking.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final Duration ttl;
  public JwtService(@Value("${security.jwt-secret:}") String secret, @Value("${security.jwt-ttl-minutes:60}") long minutes) {
    if (secret == null || secret.isBlank()) {
      byte[] generated = new byte[32]; new SecureRandom().nextBytes(generated); this.key = Keys.hmacShaKeyFor(generated);
    } else {
      if (secret.length() < 32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 characters");
      this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    this.ttl = Duration.ofMinutes(minutes);
  }
  public String issue(String email) { var now = new Date(); return Jwts.builder().subject(email).issuedAt(now).expiration(new Date(now.getTime()+ttl.toMillis())).signWith(key).compact(); }
  public String subject(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
  public long expiresInSeconds() { return ttl.toSeconds(); }
}
