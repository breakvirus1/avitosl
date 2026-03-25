package com.example.avito.response;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcategoryResponse {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static SubcategoryResponse fromEntity(Subcategory subcategory) {
        Category category = subcategory.getCategory();
        return SubcategoryResponse.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .createdAt(subcategory.getCreatedAt())
                .updatedAt(subcategory.getUpdatedAt())
                .build();
    }
}
