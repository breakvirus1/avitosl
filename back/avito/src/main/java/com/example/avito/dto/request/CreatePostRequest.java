package com.example.avito.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePostRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private Long userId;
}