package com.thomascook.hotelbooking.service;

import com.thomascook.hotelbooking.domain.*;
import com.thomascook.hotelbooking.dto.*;
import com.thomascook.hotelbooking.repository.*;
import com.thomascook.hotelbooking.exception.InvalidRequestException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service public class PasswordResetService {
  private final UserRepository users; private final PasswordResetTokenRepository tokens; private final PasswordEncoder passwords; private final ObjectProvider<JavaMailSender> mail; private final String frontendUrl; private final String from;
  public PasswordResetService(UserRepository users,PasswordResetTokenRepository tokens,PasswordEncoder passwords,ObjectProvider<JavaMailSender> mail,@Value("${app.frontend-url}") String frontendUrl,@Value("${app.mail-from}") String from){this.users=users;this.tokens=tokens;this.passwords=passwords;this.mail=mail;this.frontendUrl=frontendUrl;this.from=from;}
  @Transactional public void request(ForgotPasswordRequest request){ users.findByEmailIgnoreCase(request.email().trim()).ifPresent(user->{ byte[] bytes=new byte[32];new SecureRandom().nextBytes(bytes);String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);tokens.save(new PasswordResetToken(hash(raw),user,LocalDateTime.now().plusMinutes(30))); JavaMailSender sender=mail.getIfAvailable(); if(sender==null) throw new IllegalStateException("Mail delivery is not configured"); SimpleMailMessage message=new SimpleMailMessage();message.setFrom(from);message.setTo(user.getEmail());message.setSubject("Reset your Thomas Cook password");message.setText("Reset your password within 30 minutes: "+frontendUrl+"/reset-password?token="+raw);sender.send(message); }); }
  @Transactional public void reset(ResetPasswordRequest request){ PasswordResetToken token=tokens.findByTokenHash(hash(request.token())).orElseThrow(()->new InvalidRequestException("Reset link is invalid or expired")); if(token.isUsed()||token.getExpiresAt().isBefore(LocalDateTime.now())) throw new InvalidRequestException("Reset link is invalid or expired"); token.getUser().setPasswordHash(passwords.encode(request.newPassword())); token.setUsed(true); users.save(token.getUser()); tokens.save(token); }
  private String hash(String raw){try{return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
