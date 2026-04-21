package com.avitosl.postservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.avitosl.postservice.request.CategoryCreateRequest;
import com.avitosl.postservice.response.CategoryResponse;

import java.util.List;

@FeignClient(name = "category-service", path = "/api/categories",
             configuration = FeignConfig.class,
             fallback = CategoryServiceClientFallback.class)
public interface CategoryServiceClient {

    @GetMapping("/{id}")
    CategoryResponse getCategoryById(@PathVariable Long id);

    @GetMapping("/name/{name}")
    CategoryResponse getCategoryByName(@PathVariable String name);

    @GetMapping
    List<CategoryResponse> getAllCategories();

    @PostMapping
    CategoryResponse createCategory(@RequestBody CategoryCreateRequest request);
}