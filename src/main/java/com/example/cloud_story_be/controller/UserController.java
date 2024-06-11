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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam("user") String userJson,
                                          @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        ObjectMapper objectMapper = new ObjectMapper();
        User user;
        try {
            user = objectMapper.readValue(userJson, User.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid user data");
        }

        User savedUser = userService.saveUser(user, profileImage);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", savedUser.getId()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User user = userOpt.get();
            String token = jwtTokenProvider.generateToken(email);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "token", token,
                    "userId", user.getId(),
                    "email", user.getEmail(),
                    "nickname", user.getNickname(),
                    "profileImageUrl", user.getProfileImageUrl()
            ));
        }
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
        String userEmail = jwtTokenProvider.getUsernameFromToken(token.substring(7));
        Optional<User> userOpt = userService.findByEmail(userEmail);

        if (userOpt.isPresent()) {
            User currentUser = userOpt.get();
            ObjectMapper objectMapper = new ObjectMapper();
            User userUpdates;
            try {
                userUpdates = objectMapper.readValue(userJson, User.class);
            } catch (JsonProcessingException e) {
                return ResponseEntity.badRequest().body("Invalid user data");
            }

            if (userUpdates.getNickname() != null) {
                currentUser.setNickname(userUpdates.getNickname());
            }
            if (userUpdates.getPassword() != null) {
                currentUser.setPassword(userUpdates.getPassword());
            }

            boolean isUpdated = userService.updateUser(currentUser.getId(), currentUser.getNickname(), currentUser.getPassword(), profileImage);
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
        }
        return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "User not found"
        ));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String token) {
        String userEmail = jwtTokenProvider.getUsernameFromToken(token.substring(7));
        Optional<User> userOpt = userService.findByEmail(userEmail);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean isDeleted = userService.deleteUser(user.getId());
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
        return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "User not found"
        ));
    }
}
