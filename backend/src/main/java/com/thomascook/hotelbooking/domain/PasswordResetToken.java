package com.thomascook.hotelbooking.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="password_reset_tokens")
public class PasswordResetToken {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false,unique=true,length=64) private String tokenHash;
  @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private User user;
  @Column(nullable=false) private LocalDateTime expiresAt;
  @Column(nullable=false) private boolean used;
  protected PasswordResetToken() {}
  public PasswordResetToken(String tokenHash, User user, LocalDateTime expiresAt) { this.tokenHash=tokenHash; this.user=user; this.expiresAt=expiresAt; }
  public User getUser(){return user;} public LocalDateTime getExpiresAt(){return expiresAt;} public boolean isUsed(){return used;} public void setUsed(boolean used){this.used=used;}
}
