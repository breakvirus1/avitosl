package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Comment;
import com.avitosl.postservice.request.CommentRequest;
import com.avitosl.postservice.response.CommentResponse;
import com.avitosl.postservice.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        Comment comment = commentService.createComment(
                new Comment(null, request.getContent(), null, request.getUserId(), null, null)
        );
        return ResponseEntity.ok(mapToResponse(comment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(mapToResponse(comment));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        List<CommentResponse> responses = comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUserId(@PathVariable Long userId) {
        List<Comment> comments = commentService.getCommentsByUserId(userId);
        List<CommentResponse> responses = comments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable Long id,
                                                         @Valid @RequestBody CommentRequest request) {
        Comment commentDetails = new Comment(null, request.getContent(), null, request.getUserId(), null, null);
        Comment updatedComment = commentService.updateComment(id, commentDetails);
        return ResponseEntity.ok(mapToResponse(updatedComment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getPost() != null ? comment.getPost().getId() : null,
                comment.getUserId(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
