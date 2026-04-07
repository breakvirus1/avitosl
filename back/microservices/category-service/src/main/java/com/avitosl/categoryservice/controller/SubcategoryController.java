package com.avitosl.categoryservice.controller;

import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.request.SubcategoryRequest;
import com.avitosl.categoryservice.response.SubcategoryResponse;
import com.avitosl.categoryservice.service.SubcategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subcategories")
@RequiredArgsConstructor
public class SubcategoryController {

    private final SubcategoryService subcategoryService;

    @PostMapping
    public ResponseEntity<SubcategoryResponse> createSubcategory(@Valid @RequestBody SubcategoryRequest request) {
        Subcategory subcategory = subcategoryService.createSubcategory(
                new Subcategory(null, request.getName(), request.getDescription(),
                        new com.avitosl.categoryservice.entity.Category(request.getCategoryId(), null, null, null, null, null),
                        null, null)
        );
        return ResponseEntity.ok(mapToResponse(subcategory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> getSubcategoryById(@PathVariable Long id) {
        Subcategory subcategory = subcategoryService.getSubcategoryById(id);
        return ResponseEntity.ok(mapToResponse(subcategory));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SubcategoryResponse>> getSubcategoriesByCategoryId(@PathVariable Long categoryId) {
        List<Subcategory> subcategories = subcategoryService.getSubcategoriesByCategoryId(categoryId);
        List<SubcategoryResponse> responses = subcategories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> updateSubcategory(@PathVariable Long id,
                                                                 @Valid @RequestBody SubcategoryRequest request) {
        Subcategory subcategoryDetails = new Subcategory(null, request.getName(), request.getDescription(),
                new com.avitosl.categoryservice.entity.Category(request.getCategoryId(), null, null, null, null, null),
                null, null);
        Subcategory updatedSubcategory = subcategoryService.updateSubcategory(id, subcategoryDetails);
        return ResponseEntity.ok(mapToResponse(updatedSubcategory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Long id) {
        subcategoryService.deleteSubcategory(id);
        return ResponseEntity.noContent().build();
    }

    private SubcategoryResponse mapToResponse(Subcategory subcategory) {
        return new SubcategoryResponse(
                subcategory.getId(),
                subcategory.getName(),
                subcategory.getDescription(),
                subcategory.getCategory().getId(),
                subcategory.getCreatedAt(),
                subcategory.getUpdatedAt()
        );
    }
}
