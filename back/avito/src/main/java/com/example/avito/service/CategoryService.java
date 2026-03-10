package com.example.avito.service;

import com.example.avito.entity.Category;
import com.example.avito.mapper.CategoryMapper;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.request.CategoryRequest;
import com.example.avito.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .build();

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));
            category.setParent(parent);
        }

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    public List<CategoryResponse> getRootCategories() {
        return categoryMapper.toResponseList(categoryRepository.findByParentIsNull());
    }

    public List<CategoryResponse> getChildCategories(Long parentId) {
        return categoryMapper.toResponseList(categoryRepository.findByParentId(parentId));
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        return categoryMapper.toResponse(category);
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        category.setName(request.getName());

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Родительская категория не найдена"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}