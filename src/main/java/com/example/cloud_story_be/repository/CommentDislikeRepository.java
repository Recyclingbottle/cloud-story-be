package com.example.cloud_story_be.repository;

import com.example.cloud_story_be.entity.CommentDislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentDislikeRepository extends JpaRepository<CommentDislike, Long> {
    Optional<CommentDislike> findByCommentIdAndUserId(Long commentId, Long userId);
    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}
