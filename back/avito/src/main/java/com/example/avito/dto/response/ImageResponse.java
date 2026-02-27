package com.example.avito.dto.response;

import lombok.Data;

@Data
public class ImageResponse {
    private Long id;
    private String url;
    private PostResponse post;
}