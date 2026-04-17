package com.avitosl.postservice.response;

import com.avitosl.postservice.response.SubcategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private List<SubcategoryResponse> subcategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}