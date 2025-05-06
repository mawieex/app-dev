package com.appdev.MindFlow.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.appdev.MindFlow.model.User;
import com.appdev.MindFlow.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user); 
    List<VerificationToken> findByExpiryDateBefore(LocalDateTime now);
}