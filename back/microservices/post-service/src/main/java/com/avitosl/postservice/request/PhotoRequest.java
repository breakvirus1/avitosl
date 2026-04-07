package com.avitosl.postservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PhotoRequest {
    @NotBlank
    private String filePath;

    @NotBlank
    private String fileName;

    private Long fileSize;

    @NotBlank
    private String contentType;

    @NotNull
    private Long postId;

    private Boolean isPrimary = false;
}
