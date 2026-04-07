package com.avitosl.categoryservice.service;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.exception.ConflictException;
import com.avitosl.categoryservice.exception.NotFoundException;
import com.avitosl.categoryservice.mapper.SubcategoryMapper;
import com.avitosl.categoryservice.repository.CategoryRepository;
import com.avitosl.categoryservice.repository.SubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SubcategoryService {

    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryMapper subcategoryMapper;

    public Subcategory createSubcategory(Subcategory subcategory) {
        Category category = categoryRepository.findById(subcategory.getCategory().getId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + subcategory.getCategory().getId()));

        if (subcategoryRepository.existsByNameAndCategoryId(subcategory.getName(), category.getId())) {
            throw new ConflictException("Subcategory with name '" + subcategory.getName() + "' already exists in this category");
        }

        subcategory.setCategory(category);
        return subcategoryRepository.save(subcategory);
    }

    public Subcategory getSubcategoryById(Long id) {
        return subcategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subcategory not found with id: " + id));
    }

    public List<Subcategory> getSubcategoriesByCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        return subcategoryRepository.findByCategoryId(categoryId);
    }

    public Subcategory updateSubcategory(Long id, Subcategory subcategoryDetails) {
        Subcategory subcategory = getSubcategoryById(id);

        Category category = categoryRepository.findById(subcategoryDetails.getCategory().getId())
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + subcategoryDetails.getCategory().getId()));

        if (!subcategory.getName().equals(subcategoryDetails.getName()) &&
                subcategoryRepository.existsByNameAndCategoryId(subcategoryDetails.getName(), category.getId())) {
            throw new ConflictException("Subcategory with name '" + subcategoryDetails.getName() + "' already exists in this category");
        }

        subcategoryMapper.updateEntityFromRequest(null, subcategory);
        subcategory.setName(subcategoryDetails.getName());
        subcategory.setDescription(subcategoryDetails.getDescription());
        subcategory.setCategory(category);

        return subcategoryRepository.save(subcategory);
    }

    public void deleteSubcategory(Long id) {
        Subcategory subcategory = getSubcategoryById(id);
        subcategoryRepository.delete(subcategory);
    }
}
