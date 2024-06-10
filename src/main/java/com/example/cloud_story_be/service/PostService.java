package com.example.cloud_story_be.service;

import com.example.cloud_story_be.entity.Post;
import com.example.cloud_story_be.entity.PostPhoto;
import com.example.cloud_story_be.entity.User;
import com.example.cloud_story_be.repository.PostPhotoRepository;
import com.example.cloud_story_be.repository.PostRepository;
import com.example.cloud_story_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PostService(PostRepository postRepository, PostPhotoRepository postPhotoRepository, UserRepository userRepository, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.postPhotoRepository = postPhotoRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Post createPost(Long userId, String title, String content, List<MultipartFile> photos) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        Post post = new Post();
        post.setUser(userOpt.get());
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(post);

        List<PostPhoto> postPhotos = new ArrayList<>();
        int order = 1;
        for (MultipartFile photo : photos) {
            String fileName = fileStorageService.storeFile(photo);
            PostPhoto postPhoto = new PostPhoto();
            postPhoto.setPost(savedPost);
            postPhoto.setUrl("/uploads/" + fileName);
            postPhoto.setPhotoOrder(order++);
            postPhotos.add(postPhoto);
        }
        postPhotoRepository.saveAll(postPhotos);

        savedPost.setPhotos(postPhotos);
        return savedPost;
    }
}
