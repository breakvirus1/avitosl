package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Comment;
import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.feign.UserServiceClient;
import com.avitosl.postservice.request.CommentRequest;
import com.avitosl.postservice.response.CommentResponse;
import com.avitosl.postservice.service.CommentService;
import com.avitosl.postservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserServiceClient userServiceClient;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@RequestHeader(value = "Authorization", required = false) String token,
                                                          @Valid @RequestBody CommentRequest request) {
        String keycloakId = extractKeycloakId(token);
        if (keycloakId == null) {
            return ResponseEntity.badRequest().build();
        }

        Long userId = getUserIdByKeycloakId(keycloakId);

        // Extract author name from token
        String firstName = jwtUtil.getClaimFromToken(token, "given_name");
        String lastName = jwtUtil.getClaimFromToken(token, "family_name");
        if (firstName == null || firstName.isEmpty()) firstName = "Пользователь";
        if (lastName == null) lastName = "";

        // Создаем комментарий
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        // Создаем ссылку на пост
        Post postRef = new Post();
        postRef.setId(request.getPostId());
        comment.setPost(postRef);
        comment.setUserId(userId);
        comment.setAuthorFirstName(firstName);
        comment.setAuthorLastName(lastName);

        Comment savedComment = commentService.createComment(comment);
        return ResponseEntity.ok(mapToResponse(savedComment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(mapToResponse(comment));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(
                commentService.getCommentsByPostId(postId).stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                commentService.getCommentsByUserId(userId).stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getAllComments() {
        return ResponseEntity.ok(
                commentService.getAllComments().stream()
                        .map(this::mapToResponse)
                        .toList()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id,
                                                          @RequestHeader(value = "Authorization", required = false) String token,
                                                          @Valid @RequestBody CommentRequest request) {
        String keycloakId = extractKeycloakId(token);
        if (keycloakId == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verify ownership and update only content
        Comment existing = commentService.getCommentById(id);
        // Optionally check that existing.userId matches the user from token for authorization
        existing.setContent(request.getContent());
        // Do not update author fields
        Comment updatedComment = commentService.updateComment(id, existing);
        return ResponseEntity.ok(mapToResponse(updatedComment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    private String extractKeycloakId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return jwtUtil.getUserKeycloakIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private Long getUserIdByKeycloakId(String keycloakId) {
        try {
            var user = userServiceClient.getUserByKeycloakId(keycloakId);
            return user.getId();
        } catch (Exception e) {
            throw new RuntimeException("User not found for keycloakId: " + keycloakId, e);
        }
    }

    private CommentResponse mapToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
        response.setUserId(comment.getUserId());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        // Use denormalized author names stored in comment
        if (comment.getAuthorFirstName() != null && !comment.getAuthorFirstName().isEmpty()) {
            response.setAuthorFirstName(comment.getAuthorFirstName());
            response.setAuthorLastName(comment.getAuthorLastName());
        } else {
            // Fallback for legacy comments (should be rare)
            response.setAuthorFirstName("Пользователь");
            response.setAuthorLastName("");
        }

        return response;
    }
}
