package com.example.avito.service;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import com.example.avito.exception.ConflictException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.SubcategoryMapper;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.repository.SubcategoryRepository;
import com.example.avito.request.SubcategoryRequest;
import com.example.avito.response.SubcategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryMapper subcategoryMapper;

    public SubcategoryRepository getSubcategoryRepository() {
        return subcategoryRepository;
    }

    @Transactional
    public SubcategoryResponse createSubcategory(SubcategoryRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        if (subcategoryRepository.existsByNameAndCategoryId(request.getName(), request.getCategoryId())) {
            throw new ConflictException("Подкатегория с таким названием уже существует в данной категории");
        }

        Subcategory subcategory = subcategoryMapper.toEntity(request);
        subcategory.setCategory(category);
        
        Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
        return subcategoryMapper.toResponse(savedSubcategory);
    }

    public SubcategoryResponse getSubcategoryById(Long id) {
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подкатегория не найдена"));
        return subcategoryMapper.toResponse(subcategory);
    }

    public List<SubcategoryResponse> getAllSubcategories() {
        return subcategoryRepository.findAll().stream()
                .map(subcategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<SubcategoryResponse> getSubcategoriesByCategoryId(Long categoryId) {
        return subcategoryRepository.findByCategoryId(categoryId).stream()
                .map(subcategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubcategoryResponse updateSubcategory(Long id, SubcategoryRequest request) {
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подкатегория не найдена"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        if (subcategoryRepository.existsByNameAndCategoryIdAndIdNot(request.getName(), request.getCategoryId(), id)) {
            throw new ConflictException("Подкатегория с таким названием уже существует в данной категории");
        }

        subcategory.setName(request.getName());
        subcategory.setCategory(category);
        
        Subcategory updatedSubcategory = subcategoryRepository.save(subcategory);
        return subcategoryMapper.toResponse(updatedSubcategory);
    }

    @Transactional
    public void deleteSubcategory(Long id) {
        if (!subcategoryRepository.existsById(id)) {
            throw new NotFoundException("Подкатегория не найдена");
        }
        subcategoryRepository.deleteById(id);
    }
}
