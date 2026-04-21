package com.avitosl.postservice.request;

import lombok.Data;

@Data
public class SubcategoryCreateRequest {
    private String name;
    private String description;
    private Long categoryId;
}
