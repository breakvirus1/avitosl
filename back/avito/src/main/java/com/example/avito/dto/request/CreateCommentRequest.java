package com.example.avito.dto.request;

import lombok.Data;

@Data
public class CreateCommentRequest {
    private String content;
    private Long userId;
    private Long postId;
}