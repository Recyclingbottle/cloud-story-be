package com.example.cloud_story_be.repository;

import com.example.cloud_story_be.entity.PostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostPhotoRepository extends JpaRepository<PostPhoto, Long> {
    void deleteByPostId(Long postId);
    List<PostPhoto> findByPostId(Long postId);
}