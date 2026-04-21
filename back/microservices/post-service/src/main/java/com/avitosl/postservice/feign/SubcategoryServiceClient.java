package com.avitosl.postservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.avitosl.postservice.request.SubcategoryCreateRequest;
import com.avitosl.postservice.response.SubcategoryResponse;

import java.util.List;

@FeignClient(name = "category-service", path = "/api/subcategories",
             configuration = FeignConfig.class)
public interface SubcategoryServiceClient {

    @PostMapping
    SubcategoryResponse createSubcategory(@RequestBody SubcategoryCreateRequest request);

    @GetMapping("/category/{categoryId}")
    List<SubcategoryResponse> getByCategory(@PathVariable Long categoryId);
}
