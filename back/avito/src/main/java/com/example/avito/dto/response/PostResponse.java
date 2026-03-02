package com.example.avito.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.avito.entity.User;

@Data
public class PostResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String category;
    private User user;
}