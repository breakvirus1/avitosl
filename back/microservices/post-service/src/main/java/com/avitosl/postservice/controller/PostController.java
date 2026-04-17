package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.feign.UserServiceClient;
import com.avitosl.postservice.request.PostRequest;
import com.avitosl.postservice.request.PostUpdateRequest;
import com.avitosl.postservice.response.PostResponse;
import com.avitosl.postservice.response.UserResponse;
import com.avitosl.postservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserServiceClient userServiceClient;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setPrice(request.getPrice());
        post.setKeycloakId(request.getKeycloakId());
        post.setCategoryId(request.getCategoryId());
        post.setSubcategoryId(request.getSubcategoryId());
        post.setIsActive(true);
        post = postService.createPost(post);
        return ResponseEntity.ok(mapToResponse(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(mapToResponse(post));
    }

    @GetMapping("/user/{keycloakId}")
    public ResponseEntity<List<PostResponse>> getPostsByKeycloakId(@PathVariable String keycloakId) {
        List<Post> posts = postService.getPostsByKeycloakId(keycloakId);
        List<PostResponse> responses = posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<PostResponse>> getPostsByCategoryId(@PathVariable Long categoryId) {
        List<Post> posts = postService.getPostsByCategoryId(categoryId);
        List<PostResponse> responses = posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<PostResponse>> getActivePosts() {
        List<Post> posts = postService.getActivePosts();
        List<PostResponse> responses = posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> searchPosts(@RequestParam String title) {
        List<Post> posts = postService.searchPostsByTitle(title);
        List<PostResponse> responses = posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPostsWithPagination(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        Page<Post> posts = postService.getPostsWithPagination(pageable);
        Page<PostResponse> responses = posts.map(this::mapToResponse);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable Long id,
                                                    @RequestBody PostUpdateRequest request) {
        // Manual validation
        if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (request.getPrice() != null && request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        Post postDetails = new Post();
        if (request.getTitle() != null) postDetails.setTitle(request.getTitle());
        if (request.getDescription() != null) postDetails.setDescription(request.getDescription());
        if (request.getPrice() != null) postDetails.setPrice(request.getPrice());
        if (request.getCategoryId() != null) postDetails.setCategoryId(request.getCategoryId());
        if (request.getSubcategoryId() != null) postDetails.setSubcategoryId(request.getSubcategoryId());
        if (request.getIsActive() != null) postDetails.setIsActive(request.getIsActive());
        // keycloakId не обновляется

        Post updatedPost = postService.updatePost(id, postDetails);
        return ResponseEntity.ok(mapToResponse(updatedPost));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activatePost(@PathVariable Long id) {
        postService.activatePost(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePost(@PathVariable Long id) {
        postService.deactivatePost(id);
        return ResponseEntity.ok().build();
    }

    private PostResponse mapToResponse(Post post) {
        UserResponse author = null;
        try {
            author = userServiceClient.getUserByKeycloakId(post.getKeycloakId());
        } catch (Exception e) {
            // Если не удалось получить пользователя, оставляем null
        }
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getPrice(),
                post.getKeycloakId(),
                author,
                post.getCategoryId(),
                post.getSubcategoryId(),
                post.getIsActive(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPhotos()
        );
    }
}
