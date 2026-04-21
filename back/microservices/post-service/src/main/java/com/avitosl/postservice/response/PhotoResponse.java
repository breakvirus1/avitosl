package com.avitosl.postservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private Long id;
    private String filePath;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private Long postId;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
