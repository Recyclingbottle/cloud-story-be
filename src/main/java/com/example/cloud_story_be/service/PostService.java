package com.example.cloud_story_be.service;

import com.example.cloud_story_be.entity.*;
import com.example.cloud_story_be.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PostLikeRepository postLikeRepository;
    private final PostDislikeRepository postDislikeRepository;

    @Autowired
    public PostService(PostRepository postRepository, PostPhotoRepository postPhotoRepository,
                       PostLikeRepository postLikeRepository, PostDislikeRepository postDislikeRepository,
                       UserRepository userRepository, FileStorageService fileStorageService) {
        this.postRepository = postRepository;
        this.postPhotoRepository = postPhotoRepository;
        this.postLikeRepository = postLikeRepository;
        this.postDislikeRepository = postDislikeRepository;
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
    @Transactional
    public Optional<Post> getPostById(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        post.ifPresent(p -> {
            p.setViewCount(p.getViewCount() + 1);
            postRepository.save(p);
        });
        return post;
    }
    public Page<Post> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional
    public boolean likePost(Long postId, Long userId) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isPresent()) {
            return false;
        }
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUser(user);

        postLikeRepository.save(postLike);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
        return true;
    }

    @Transactional
    public boolean unlikePost(Long postId, Long userId) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isEmpty()) {
            return false;
        }
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setLikeCount(post.getLikeCount() - 1);
        postRepository.save(post);
        return true;
    }

    @Transactional
    public boolean dislikePost(Long postId, Long userId) {
        Optional<PostDislike> existingDislike = postDislikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingDislike.isPresent()) {
            return false;
        }
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        PostDislike postDislike = new PostDislike();
        postDislike.setPost(post);
        postDislike.setUser(user);

        postDislikeRepository.save(postDislike);
        post.setDislikeCount(post.getDislikeCount() + 1);
        postRepository.save(post);
        return true;
    }

    @Transactional
    public boolean undislikePost(Long postId, Long userId) {
        Optional<PostDislike> existingDislike = postDislikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingDislike.isEmpty()) {
            return false;
        }
        postDislikeRepository.deleteByPostIdAndUserId(postId, userId);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        post.setDislikeCount(post.getDislikeCount() - 1);
        postRepository.save(post);
        return true;
    }
    @Transactional
    public List<Post> getTodayPopularPosts() {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        List<Post> posts = postRepository.findAllByCreatedAtAfter(startOfDay);
        return calculatePopularPosts(posts, 1.5);
    }

    @Transactional
    public List<Post> getWeekPopularPosts() {
        LocalDateTime startOfWeek = LocalDateTime.now().with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        List<Post> posts = postRepository.findAllByCreatedAtAfter(startOfWeek);
        return calculatePopularPosts(posts, 1.2);
    }

    private List<Post> calculatePopularPosts(List<Post> posts, double timeWeight) {
        return posts.stream()
                .sorted((p1, p2) -> Double.compare(calculateScore(p2, timeWeight), calculateScore(p1, timeWeight)))
                .collect(Collectors.toList());
    }

    private double calculateScore(Post post, double timeWeight) {
        return (post.getViewCount() + (post.getLikeCount() * 2) - (post.getDislikeCount() * 1)) * timeWeight;
    }
    @Transactional
    public boolean updatePost(Long postId, Long userId, String title, String content, List<MultipartFile> photos) throws IOException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        post.setTitle(title);
        post.setContent(content);
        post.setUpdatedAt(LocalDateTime.now());

        if (photos != null && !photos.isEmpty()) {
            postPhotoRepository.deleteByPostId(postId);
            List<PostPhoto> postPhotos = new ArrayList<>();
            int order = 1;
            for (MultipartFile photo : photos) {
                String fileName = fileStorageService.storeFile(photo);
                PostPhoto postPhoto = new PostPhoto();
                postPhoto.setPost(post);
                postPhoto.setUrl("/uploads/" + fileName);
                postPhoto.setPhotoOrder(order++);
                postPhotos.add(postPhoto);
            }
            post.getPhotos().clear();
            post.getPhotos().addAll(postPhotos);
        }

        postRepository.save(post);
        return true;
    }
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        postRepository.delete(post);
        return true;
    }
}
