package com.example.avito.controller;

import com.example.avito.entity.Comment;
import com.example.avito.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@Tag(name = "Comment Controller", description = "операции с комментариями")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Operation(summary = "создание нового коммента")
    @PostMapping
    public Comment createComment(@RequestBody Comment comment) {
        return commentService.createComment(comment);
    }

    @Operation(summary = "поиск комментов по id объявления")
    @GetMapping("/post/{postId}")
    public List<Comment> findByPostId(@PathVariable Long postId) {
        return commentService.findByPostId(postId);
    }

    @Operation(summary = "удаление коммента по id")
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }
}