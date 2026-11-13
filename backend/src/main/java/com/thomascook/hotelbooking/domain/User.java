package com.thomascook.hotelbooking.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
public class User {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, unique = true, length = 254) private String email;
  @Column(nullable = false, length = 100) private String passwordHash;
  @Column(nullable = false, length = 150) private String displayName;
  @CreationTimestamp @Column(nullable = false, updatable = false) private LocalDateTime createdAt;
  protected User() {}
  public User(String email, String passwordHash, String displayName) { this.email=email; this.passwordHash=passwordHash; this.displayName=displayName; }
  public Long getId() { return id; }
  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
