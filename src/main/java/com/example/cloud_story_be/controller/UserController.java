package com.example.cloud_story_be.controller;

import com.example.cloud_story_be.entity.User;
import com.example.cloud_story_be.security.JwtTokenProvider;
import com.example.cloud_story_be.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private Long getUserIdFromToken(String token) {
        String userEmail = jwtTokenProvider.getUsernameFromToken(token.substring(7));
        return userService.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam("user") String userJson,
                                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            User user = objectMapper.readValue(userJson, User.class);
            User savedUser = userService.saveUser(user, profileImage);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", savedUser.getId()
            ));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid user data");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        Logger logger = LoggerFactory.getLogger(UserController.class);

        // Log the received login request
        logger.info("Received login request: {}", loginRequest);

        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();

            // Log the user details
            logger.info("User found: {}", user);

            // Check for null values
            String token = jwtTokenProvider.generateToken(email);
            Long userId = user.getId();
            String userEmail = user.getEmail();
            String nickname = user.getNickname();
            String profileImageUrl = user.getProfileImageUrl();

            if (userId == null || userEmail == null || nickname == null || profileImageUrl == null) {
                logger.error("Null value found in user details: userId={}, email={}, nickname={}, profileImageUrl={}",
                        userId, userEmail, nickname, profileImageUrl);
                return ResponseEntity.status(500).body(Map.of(
                        "success", false,
                        "message", "Server error: Null value found in user details"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", token,
                    "userId", userId,
                    "email", userEmail,
                    "nickname", nickname,
                    "profileImageUrl", profileImageUrl
            ));
        }

        logger.warn("Invalid login attempt: email={}", email);
        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Invalid email or password"
        ));
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> emailRequest) {
        String email = emailRequest.get("email");
        if (userService.existsByEmail(email)) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "Email already in use"
            ));
        }
        userService.sendVerificationCode(email);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Verification code sent to email"
        ));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam("nickname") String nickname) {
        if (userService.existsByNickname(nickname)) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "available", false,
                    "message", "Nickname already in use"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "available", true
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> verificationRequest) {
        String email = verificationRequest.get("email");
        String code = verificationRequest.get("verificationCode");
        boolean isVerified = userService.verifyEmail(email, code);
        if (isVerified) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully"
            ));
        } else {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Invalid verification code"
            ));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestHeader("Authorization") String token,
                                        @RequestParam("user") String userJson,
                                        @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            Long userId = getUserIdFromToken(token);
            ObjectMapper objectMapper = new ObjectMapper();
            User userUpdates = objectMapper.readValue(userJson, User.class);
            boolean isUpdated = userService.updateUser(userId, userUpdates.getNickname(), userUpdates.getPassword(), profileImage);
            if (isUpdated) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "User information updated successfully"
                ));
            }
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Invalid request data"
            ));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid user data");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        boolean isDeleted = userService.deleteUser(userId);
        if (isDeleted) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User account deleted successfully"
            ));
        }
        return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Failed to delete user account"
        ));
    }
}
