package com.avitosl.postservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private Long postId;
    private Long userId;
    private String authorFirstName;
    private String authorLastName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
