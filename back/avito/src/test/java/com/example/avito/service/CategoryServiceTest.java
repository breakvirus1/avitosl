package com.example.avito.service;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import com.example.avito.exception.ConflictException;
import com.example.avito.exception.NotFoundException;
import com.example.avito.mapper.CategoryMapper;
import com.example.avito.mapper.SubcategoryMapper;
import com.example.avito.repository.CategoryRepository;
import com.example.avito.repository.SubcategoryRepository;
import com.example.avito.request.CategoryRequest;
import com.example.avito.response.CategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private SubcategoryMapper subcategoryMapper;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_shouldCreateCategorySuccessfully() {
        // Given
        CategoryRequest request = CategoryRequest.builder()
                .name("Test Category")
                .subcategoryNames(List.of("Sub1", "Sub2"))
                .build();

        Category category = Category.builder().name("Test Category").build();
        Category savedCategory = Category.builder().id(1L).name("Test Category").build();
        Subcategory subcategory = new Subcategory();
        subcategory.setName("Sub1");
        subcategory.setCategory(savedCategory);
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Test Category").build();

        when(categoryRepository.existsByName(anyString())).thenReturn(false);
        doReturn(savedCategory).when(categoryRepository).save(any(Category.class));
        doReturn(subcategory).when(subcategoryRepository).save(any(Subcategory.class));
        lenient().when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        // When
        CategoryResponse result = categoryService.createCategory(request);

        // Then
        assertThat(result).isEqualTo(response);
        verify(categoryRepository).save(any(Category.class));
        verify(subcategoryRepository, times(2)).save(any());
    }

    @Test
    void createCategory_shouldThrowConflictExceptionWhenNameExists() {
        // Given
        CategoryRequest request = CategoryRequest.builder().name("Existing Category").build();

        when(categoryRepository.existsByName("Existing Category")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Категория с таким названием уже существует");
    }

    @Test
    void getAllCategories_shouldReturnCategories() {
        // Given
        List<Category> categories = List.of(new Category(), new Category());
        List<CategoryResponse> responses = List.of(new CategoryResponse(), new CategoryResponse());

        when(categoryRepository.findAll()).thenReturn(categories);
        lenient().when(subcategoryMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());
        when(categoryMapper.toResponseList(categories)).thenReturn(responses);

        // When
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Then
        assertThat(result).isEqualTo(responses);
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        // Given
        Category category = Category.builder().id(1L).name("Test").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Test").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        lenient().when(subcategoryMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());
        when(categoryMapper.toResponse(category)).thenReturn(response);

        // When
        CategoryResponse result = categoryService.getCategoryById(1L);

        // Then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void getCategoryById_shouldThrowNotFoundExceptionWhenNotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена");
    }

    @Test
    void updateCategory_shouldUpdateCategorySuccessfully() {
        // Given
        CategoryRequest request = CategoryRequest.builder()
                .name("Updated Category")
                .subcategoryNames(List.of("New Sub"))
                .build();

        Category existingCategory = Category.builder().id(1L).name("Old Category").build();
        Category updatedCategory = Category.builder().id(1L).name("Updated Category").build();
        CategoryResponse response = CategoryResponse.builder().id(1L).name("Updated Category").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByNameAndIdNot("Updated Category", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(response);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, request);

        // Then
        assertThat(result).isEqualTo(response);
        verify(subcategoryRepository).deleteByCategoryId(1L);
        verify(subcategoryRepository).save(any());
    }

    @Test
    void updateCategory_shouldThrowConflictExceptionWhenNameExists() {
        // Given
        CategoryRequest request = CategoryRequest.builder().name("Existing Name").build();
        Category category = Category.builder().id(1L).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameAndIdNot("Existing Name", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Категория с таким названием уже существует");
    }

    @Test
    void deleteCategory_shouldDeleteCategory() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(true);

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_shouldThrowNotFoundExceptionWhenNotFound() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена");
    }
}