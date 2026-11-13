package com.thomascook.hotelbooking.repository;
import com.thomascook.hotelbooking.domain.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken,Long> { Optional<PasswordResetToken> findByTokenHash(String tokenHash); }
