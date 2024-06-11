package com.example.cloud_story_be.controller;

import com.example.cloud_story_be.entity.Post;
import com.example.cloud_story_be.security.JwtTokenProvider;
import com.example.cloud_story_be.service.PostService;
import com.example.cloud_story_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private Long getUserIdFromToken(String token) {
        String userEmail = jwtTokenProvider.getUsernameFromToken(token.substring(7));
        return userService.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestHeader("Authorization") String token,
                                        @RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam("photos") List<MultipartFile> photos) {
        try {
            Long userId = getUserIdFromToken(token);
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

    @GetMapping
    public ResponseEntity<?> getAllPosts(@RequestParam(name = "page", defaultValue = "1") int page,
                                         @RequestParam(name = "limit", defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> postsPage = postService.getAllPosts(pageable);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "currentPage", postsPage.getNumber() + 1,
                "totalPages", postsPage.getTotalPages(),
                "totalPosts", postsPage.getTotalElements(),
                "posts", postsPage.getContent()
        ));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable("postId") Long postId) {
        Optional<Post> post = postService.getPostById(postId);
        return post.map(value -> ResponseEntity.ok(Map.of(
                "success", true,
                "post", value
        ))).orElseGet(() -> ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "Post not found"
        )));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@RequestHeader("Authorization") String token, @PathVariable("postId") Long postId) {
        return handleLikeOrDislike(token, postId, true, true);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlikePost(@RequestHeader("Authorization") String token, @PathVariable("postId") Long postId) {
        return handleLikeOrDislike(token, postId, true, false);
    }

    @PostMapping("/{postId}/dislike")
    public ResponseEntity<?> dislikePost(@RequestHeader("Authorization") String token, @PathVariable("postId") Long postId) {
        return handleLikeOrDislike(token, postId, false, true);
    }

    @DeleteMapping("/{postId}/dislike")
    public ResponseEntity<?> undislikePost(@RequestHeader("Authorization") String token, @PathVariable("postId") Long postId) {
        return handleLikeOrDislike(token, postId, false, false);
    }

    private ResponseEntity<?> handleLikeOrDislike(String token, Long postId, boolean isLike, boolean isAdd) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success;
            if (isLike) {
                success = isAdd ? postService.likePost(postId, userId) : postService.unlikePost(postId, userId);
            } else {
                success = isAdd ? postService.dislikePost(postId, userId) : postService.undislikePost(postId, userId);
            }

            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", isLike ? (isAdd ? "Post liked successfully" : "Post like removed successfully")
                                : (isAdd ? "Post disliked successfully" : "Post dislike removed successfully")
                ));
            } else {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", isLike ? (isAdd ? "Post already liked by user" : "Post not liked by user")
                                : (isAdd ? "Post already disliked by user" : "Post not disliked by user")
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/popular/today")
    public ResponseEntity<?> getTodayPopularPosts() {
        try {
            List<Post> popularPosts = postService.getTodayPopularPosts();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "posts", popularPosts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while fetching today's popular posts"
            ));
        }
    }

    @GetMapping("/popular/week")
    public ResponseEntity<?> getWeekPopularPosts() {
        try {
            List<Post> popularPosts = postService.getWeekPopularPosts();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "posts", popularPosts
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while fetching this week's popular posts"
            ));
        }
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(@RequestHeader("Authorization") String token,
                                        @PathVariable("postId") Long postId,
                                        @RequestParam("title") String title,
                                        @RequestParam("content") String content,
                                        @RequestParam(value = "photos", required = false) List<MultipartFile> photos) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = postService.updatePost(postId, userId, title, content, photos);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Post updated successfully"
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Post not found or not authorized"
                ));
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while updating the post"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@RequestHeader("Authorization") String token, @PathVariable("postId") Long postId) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = postService.deletePost(postId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Post deleted successfully"
                ));
            } else {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "Post could not be deleted"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
