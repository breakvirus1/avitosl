package com.example.avito.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

import com.example.avito.entity.User;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private User user;
    private PostResponse post;
}