package com.example.cloud_story_be.repository;

import com.example.cloud_story_be.entity.PostDislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostDislikeRepository extends JpaRepository<PostDislike, Long> {
    Optional<PostDislike> findByPostIdAndUserId(Long postId, Long userId);
    void deleteByPostIdAndUserId(Long postId, Long userId);
}
