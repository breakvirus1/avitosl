package com.example.avito.response;

import com.example.avito.entity.Photo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private String text;
    private UserResponse author;
    private Long postId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}