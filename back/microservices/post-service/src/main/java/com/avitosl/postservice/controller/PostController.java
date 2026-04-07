package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.request.PostRequest;
import com.avitosl.postservice.response.PostResponse;
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

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        Post post = postService.createPost(
                new Post(null, request.getTitle(), request.getDescription(), request.getPrice(),
                        request.getUserId(), request.getCategoryId(), request.getSubcategoryId(),
                        true, null, null, null, null)
        );
        return ResponseEntity.ok(mapToResponse(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(mapToResponse(post));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostResponse>> getPostsByUserId(@PathVariable Long userId) {
        List<Post> posts = postService.getPostsByUserId(userId);
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
                                                    @Valid @RequestBody PostRequest request) {
        Post postDetails = new Post(null, request.getTitle(), request.getDescription(), request.getPrice(),
                request.getUserId(), request.getCategoryId(), request.getSubcategoryId(),
                true, null, null, null, null);
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
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getDescription(),
                post.getPrice(),
                post.getUserId(),
                post.getCategoryId(),
                post.getSubcategoryId(),
                post.getIsActive(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
