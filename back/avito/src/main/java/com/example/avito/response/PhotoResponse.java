package com.example.avito.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoResponse {
    private Long id;
    private String url;
    private boolean primary;
    private Long postId;
    private LocalDateTime createdAt;
}