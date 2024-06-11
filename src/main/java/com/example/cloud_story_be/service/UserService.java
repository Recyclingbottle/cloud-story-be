package com.example.cloud_story_be.service;

import com.example.cloud_story_be.entity.User;
import com.example.cloud_story_be.entity.EmailVerification;
import com.example.cloud_story_be.util.VerificationCodeGenerator;
import com.example.cloud_story_be.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder, EmailService emailService, EmailVerificationRepository emailVerificationRepository, FileStorageService fileStorageService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.emailVerificationRepository = emailVerificationRepository;
        this.fileStorageService = fileStorageService;
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new Object[]{email}, new UserRowMapper());
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<User> findByNickname(String nickname) {
        String sql = "SELECT * FROM users WHERE nickname = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new Object[]{nickname}, new UserRowMapper());
            return Optional.of(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{email}, Integer.class);
        return count != null && count > 0;
    }

    public boolean existsByNickname(String nickname) {
        String sql = "SELECT COUNT(*) FROM users WHERE nickname = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{nickname}, Integer.class);
        return count != null && count > 0;
    }

    @Transactional
    public User saveUser(User user, MultipartFile profileImage) {
        String fileName = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            fileName = fileStorageService.storeFile(profileImage);
            user.setProfileImageUrl("/uploads/" + fileName);
        }
        String sql = "INSERT INTO users (email, password, nickname, profile_image_url, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        jdbcTemplate.update(sql, user.getEmail(), encodedPassword, user.getNickname(), user.getProfileImageUrl(), new Date(), new Date());
        return findByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("User not found after save"));
    }

    public boolean sendVerificationCode(String email) {
        if (existsByEmail(email)) {
            return false;
        }
        Optional<EmailVerification> existingVerification = emailVerificationRepository.findByEmail(email);
        if (existingVerification.isPresent()) {
            emailVerificationRepository.delete(existingVerification.get());
        }
        String code = VerificationCodeGenerator.generateCode();
        String sql = "INSERT INTO email_verification (email, verification_code, created_at, verified) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, email, code, new Date(), false);
        emailService.sendVerificationCode(email, code);
        return true;
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setNickname(rs.getString("nickname"));
            user.setProfileImageUrl(rs.getString("profile_image_url"));
            user.setCreatedAt(rs.getTimestamp("created_at"));
            user.setUpdatedAt(rs.getTimestamp("updated_at"));
            return user;
        }
    }

    @Transactional
    public boolean updateUser(Long userId, String nickname, String password, MultipartFile profileImage) {
        StringBuilder sql = new StringBuilder("UPDATE users SET updated_at = ?");
        if (nickname != null && !nickname.isEmpty()) {
            sql.append(", nickname = '").append(nickname).append("'");
        }
        if (password != null && !password.isEmpty()) {
            sql.append(", password = '").append(passwordEncoder.encode(password)).append("'");
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            String fileName = fileStorageService.storeFile(profileImage);
            sql.append(", profile_image_url = '").append("/uploads/").append(fileName).append("'");
        }
        sql.append(" WHERE id = ?");
        int rows = jdbcTemplate.update(sql.toString(), new Date(), userId);
        return rows > 0;
    }


    @Transactional
    public boolean deleteUser(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rows = jdbcTemplate.update(sql, userId);
        return rows > 0;
    }

    @Transactional
    public boolean verifyEmail(String email, String code) {
        Optional<EmailVerification> optionalVerification = emailVerificationRepository.findByEmailAndVerificationCode(email, code);
        if (optionalVerification.isPresent()) {
            EmailVerification verification = optionalVerification.get();
            verification.setVerified(true);
            verification.setVerifiedAt(new Date());
            emailVerificationRepository.save(verification);
            return true;
        }
        return false;
    }
}
