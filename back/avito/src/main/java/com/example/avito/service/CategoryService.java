package com.example.avito.service;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import com.example.avito.exception.ConflictException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.CategoryMapper;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.repository.SubcategoryRepository;
import com.example.avito.request.CategoryRequest;
import com.example.avito.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final SubcategoryRepository subcategoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Категория с таким названием уже существует");
        }

        Category category = Category.builder()
                .name(request.getName())
                .build();

        Category savedCategory = categoryRepository.save(category);

        // Если переданы подкатегории, создаем их
        if (request.getSubcategoryNames() != null && !request.getSubcategoryNames().isEmpty()) {
            request.getSubcategoryNames().forEach(subcategoryName -> {
                Subcategory subcategory = Subcategory.builder()
                        .name(subcategoryName)
                        .category(savedCategory)
                        .build();
                subcategoryRepository.save(subcategory);
            });
        }

        return categoryMapper.toResponse(savedCategory);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ConflictException("Категория с таким названием уже существует");
        }

        category.setName(request.getName());
        categoryRepository.save(category);

        // Обработка подкатегорий (удаление старых и создание новых)
        if (request.getSubcategoryNames() != null) {
            // Удаляем все существующие подкатегории
            subcategoryRepository.deleteByCategoryId(id);
            
            // Создаем новые подкатегории
            request.getSubcategoryNames().forEach(subcategoryName -> {
                Subcategory subcategory = Subcategory.builder()
                        .name(subcategoryName)
                        .category(category)
                        .build();
                subcategoryRepository.save(subcategory);
            });
        }

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Категория не найдена");
        }
        categoryRepository.deleteById(id);
    }
}
