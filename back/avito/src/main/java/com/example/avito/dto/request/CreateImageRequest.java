package com.example.avito.dto.request;

import lombok.Data;

@Data
public class CreateImageRequest {
    private String url;
    private Long postId;
}