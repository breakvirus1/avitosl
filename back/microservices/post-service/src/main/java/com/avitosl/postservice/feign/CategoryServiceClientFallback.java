package com.avitosl.postservice.feign;

import org.springframework.stereotype.Component;
import com.avitosl.postservice.response.CategoryResponse;

@Component
public class CategoryServiceClientFallback implements CategoryServiceClient {

    @Override
    public CategoryResponse getCategoryById(Long id) {
        throw new RuntimeException("Category service is unavailable. Cannot fetch category with id: " + id);
    }

    @Override
    public CategoryResponse getCategoryByName(String name) {
        throw new RuntimeException("Category service is unavailable. Cannot fetch category with name: " + name);
    }
}