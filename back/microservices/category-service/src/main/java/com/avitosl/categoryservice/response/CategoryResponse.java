package com.avitosl.categoryservice.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private List<SubcategoryResponse> subcategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
