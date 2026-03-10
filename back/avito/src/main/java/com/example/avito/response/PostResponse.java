package com.example.avito.response;

import com.example.avito.entity.Photo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private boolean active;
    private UserResponse author;
    private List<CategoryResponse> categories;
    private List<Photo> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}