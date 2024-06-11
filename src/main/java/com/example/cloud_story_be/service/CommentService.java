package com.example.cloud_story_be.service;

import com.example.cloud_story_be.entity.*;
import com.example.cloud_story_be.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentDislikeRepository commentDislikeRepository;
    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, CommentLikeRepository commentLikeRepository, CommentDislikeRepository commentDislikeRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentDislikeRepository = commentDislikeRepository;
        this.commentLikeRepository = commentLikeRepository;
    }
    public Page<Comment> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPostId(postId, pageable);
    }
    @Transactional
    public Comment addComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(java.time.LocalDateTime.now());
        comment.setUpdatedAt(java.time.LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        // 댓글 수 증가
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return savedComment;
    }

    @Transactional
    public boolean deleteComment(Long postId, Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        commentRepository.delete(comment);

        // 댓글 수 감소
        post.setCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);

        return true;
    }
    @Transactional
    public Comment updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized access");
        }

        comment.setContent(content);
        comment.setUpdatedAt(java.time.LocalDateTime.now());
        return commentRepository.save(comment);
    }
    @Transactional
    public boolean likeComment(Long commentId, Long userId) {
        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
        if (existingLike.isPresent()) {
            return false;
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        CommentLike commentLike = new CommentLike();
        commentLike.setComment(comment);
        commentLike.setUser(user);

        commentLikeRepository.save(commentLike);
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentRepository.save(comment);
        return true;
    }

    @Transactional
    public boolean unlikeComment(Long commentId, Long userId) {
        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);
        if (existingLike.isEmpty()) {
            return false;
        }
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setLikeCount(comment.getLikeCount() - 1);
        commentRepository.save(comment);
        return true;
    }
    @Transactional
    public boolean dislikeComment(Long commentId, Long userId) {
        Optional<CommentDislike> existingDislike = commentDislikeRepository.findByCommentIdAndUserId(commentId, userId);
        if (existingDislike.isPresent()) {
            return false;  // 이미 싫어요가 되어 있는 경우
        }
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        CommentDislike commentDislike = new CommentDislike();
        commentDislike.setComment(comment);
        commentDislike.setUser(user);

        commentDislikeRepository.save(commentDislike);
        comment.setDislikeCount(comment.getDislikeCount() + 1);
        commentRepository.save(comment);
        return true;
    }

    @Transactional
    public boolean undislikeComment(Long commentId, Long userId) {
        Optional<CommentDislike> existingDislike = commentDislikeRepository.findByCommentIdAndUserId(commentId, userId);
        if (existingDislike.isEmpty()) {
            return false;
        }
        commentDislikeRepository.deleteByCommentIdAndUserId(commentId, userId);
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setDislikeCount(comment.getDislikeCount() - 1);
        commentRepository.save(comment);
        return true;
    }
}
