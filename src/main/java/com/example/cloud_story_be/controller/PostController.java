package com.example.cloud_story_be.controller;

import com.example.cloud_story_be.entity.Post;
import com.example.cloud_story_be.security.JwtTokenProvider;
import com.example.cloud_story_be.service.PostService;
import com.example.cloud_story_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.postService = postService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestHeader("Authorization") String token,
                                        @RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("photos") List<MultipartFile> photos) {
        try {
            String userEmail = jwtTokenProvider.getUsernameFromToken(token.substring(7));
            Long userId = userService.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found")).getId();

            Post post = postService.createPost(userId, title, content, photos);
            return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "postId", post.getId(),
                    "message", "Post created successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while creating the post"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
