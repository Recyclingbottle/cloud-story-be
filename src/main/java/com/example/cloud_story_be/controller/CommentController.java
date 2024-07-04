package com.example.cloud_story_be.controller;

import com.example.cloud_story_be.entity.Comment;
import com.example.cloud_story_be.security.JwtTokenProvider;
import com.example.cloud_story_be.service.CommentService;
import com.example.cloud_story_be.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Autowired
    public CommentController(CommentService commentService, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.commentService = commentService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    private Long getUserIdFromToken(String token) {
        String userEmail = jwtTokenProvider.getUsernameFromToken(token.substring(7));
        return userService.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found")).getId();
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String token,
                                        @PathVariable("postId") Long postId,
                                        @RequestBody Map<String, String> commentRequest) {
        String content = commentRequest.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Content cannot be empty"
            ));
        }

        try {
            Long userId = getUserIdFromToken(token);
            Comment comment = commentService.addComment(postId, userId, content);
            return ResponseEntity.status(201).body(Map.of(
                    "success", true,
                    "commentId", comment.getId(),
                    "message", "Comment added successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while adding the comment"
            ));
        }
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@RequestHeader("Authorization") String token,
                                           @PathVariable("postId") Long postId,
                                           @PathVariable("commentId") Long commentId) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = commentService.deleteComment(postId, commentId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Comment deleted successfully"
                ));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "Comment not found"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while deleting the comment"
            ));
        }
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@RequestHeader("Authorization") String token,
                                           @PathVariable("postId") Long postId,
                                           @PathVariable("commentId") Long commentId,
                                           @RequestBody Map<String, String> commentRequest) {
        String content = commentRequest.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Content cannot be empty"
            ));
        }

        try {
            Long userId = getUserIdFromToken(token);
            Comment updatedComment = commentService.updateComment(commentId, userId, content);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Comment updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error while updating the comment"
            ));
        }
    }

    @PostMapping("/{postId}/comments/{commentId}/like")
    public ResponseEntity<?> likeComment(@RequestHeader("Authorization") String token,
                                         @PathVariable("postId") Long postId,
                                         @PathVariable("commentId") Long commentId) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = commentService.likeComment(commentId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Comment liked successfully"
                ));
            } else {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "Comment already liked by user"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{postId}/comments/{commentId}/like")
    public ResponseEntity<?> unlikeComment(@RequestHeader("Authorization") String token,
                                           @PathVariable("postId") Long postId,
                                           @PathVariable("commentId") Long commentId) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = commentService.unlikeComment(commentId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Comment like removed successfully"
                ));
            } else {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "Comment not liked by user"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/{postId}/comments/{commentId}/dislike")
    public ResponseEntity<?> dislikeComment(@RequestHeader("Authorization") String token,
                                            @PathVariable("postId") Long postId,
                                            @PathVariable("commentId") Long commentId) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = commentService.dislikeComment(commentId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Comment disliked successfully"
                ));
            } else {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "Comment already disliked by user"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{postId}/comments/{commentId}/dislike")
    public ResponseEntity<?> undislikeComment(@RequestHeader("Authorization") String token,
                                              @PathVariable("postId") Long postId,
                                              @PathVariable("commentId") Long commentId) {
        try {
            Long userId = getUserIdFromToken(token);
            boolean success = commentService.undislikeComment(commentId, userId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Comment dislike removed successfully"
                ));
            } else {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "Comment not disliked by user"
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable("postId") Long postId,
                                                 @RequestParam(name = "page", defaultValue = "1") int page,
                                                 @RequestParam(name = "limit", defaultValue = "10") int limit,
                                                 @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
                                                 @RequestParam(name = "direction", defaultValue = "desc") String direction) {
        Sort sortOrder = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page - 1, limit, sortOrder);
        Page<Comment> commentsPage = commentService.getCommentsByPostId(postId, pageable);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "currentPage", commentsPage.getNumber() + 1,
                "totalPages", commentsPage.getTotalPages(),
                "totalComments", commentsPage.getTotalElements(),
                "comments", commentsPage.getContent()
        ));
    }
}
