package com.example.avito.response;

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
    private CategoryResponse category;
    private SubcategoryResponse subcategory;
    private List<PhotoResponse> photos;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
