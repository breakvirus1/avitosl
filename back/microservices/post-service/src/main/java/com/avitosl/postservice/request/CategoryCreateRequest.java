package com.avitosl.postservice.request;

import lombok.Data;

@Data
public class CategoryCreateRequest {
    private String name;
    private String description;
}
