package com.example.cloud_story_be.repository;

import com.example.cloud_story_be.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmail(String email);
    Optional<EmailVerification> findByEmailAndVerificationCode(String email, String verificationCode);
}
