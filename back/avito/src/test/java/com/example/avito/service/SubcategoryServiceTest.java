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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubcategoryServiceTest {

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryMapper subcategoryMapper;

    @InjectMocks
    private SubcategoryService subcategoryService;

    @Test
    void createSubcategory_shouldCreateSubcategorySuccessfully() {
        // Given
        SubcategoryRequest request = SubcategoryRequest.builder()
                .name("Test Subcategory")
                .categoryId(1L)
                .build();

        Category category = Category.builder().id(1L).build();
        Subcategory subcategory = Subcategory.builder().name("Test Subcategory").build();
        Subcategory savedSubcategory = Subcategory.builder().id(1L).name("Test Subcategory").build();
        SubcategoryResponse response = SubcategoryResponse.builder().id(1L).name("Test Subcategory").build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryId(anyString(), anyLong())).thenReturn(false);
        when(subcategoryMapper.toEntity(request)).thenReturn(subcategory);
        when(subcategoryRepository.save(any(Subcategory.class))).thenReturn(savedSubcategory);
        when(subcategoryMapper.toResponse(savedSubcategory)).thenReturn(response);

        // When
        SubcategoryResponse result = subcategoryService.createSubcategory(request);

        // Then
        assertThat(result).isEqualTo(response);
        verify(subcategoryRepository).save(any(Subcategory.class));
    }

    @Test
    void createSubcategory_shouldThrowNotFoundExceptionWhenCategoryNotFound() {
        // Given
        SubcategoryRequest request = SubcategoryRequest.builder().categoryId(1L).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subcategoryService.createSubcategory(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Категория не найдена");
    }

    @Test
    void createSubcategory_shouldThrowConflictExceptionWhenNameExists() {
        // Given
        SubcategoryRequest request = SubcategoryRequest.builder()
                .name("Existing Subcategory")
                .categoryId(1L)
                .build();

        Category category = Category.builder().id(1L).build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryId("Existing Subcategory", 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> subcategoryService.createSubcategory(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Подкатегория с таким названием уже существует в данной категории");
    }

    @Test
    void getSubcategoryById_shouldReturnSubcategory() {
        // Given
        Subcategory subcategory = Subcategory.builder().id(1L).name("Test").build();
        SubcategoryResponse response = SubcategoryResponse.builder().id(1L).name("Test").build();

        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory));
        when(subcategoryMapper.toResponse(subcategory)).thenReturn(response);

        // When
        SubcategoryResponse result = subcategoryService.getSubcategoryById(1L);

        // Then
        assertThat(result).isEqualTo(response);
    }

    @Test
    void getSubcategoryById_shouldThrowNotFoundExceptionWhenNotFound() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> subcategoryService.getSubcategoryById(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Подкатегория не найдена");
    }

    @Test
    void getAllSubcategories_shouldReturnSubcategories() {
        // Given
        List<Subcategory> subcategories = List.of(new Subcategory(), new Subcategory());
        List<SubcategoryResponse> responses = List.of(new SubcategoryResponse(), new SubcategoryResponse());

        when(subcategoryRepository.findAll()).thenReturn(subcategories);
        when(subcategoryMapper.toResponse(any(Subcategory.class))).thenReturn(new SubcategoryResponse());

        // When
        List<SubcategoryResponse> result = subcategoryService.getAllSubcategories();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void getSubcategoriesByCategoryId_shouldReturnSubcategories() {
        // Given
        List<Subcategory> subcategories = List.of(new Subcategory(), new Subcategory());
        List<SubcategoryResponse> responses = List.of(new SubcategoryResponse(), new SubcategoryResponse());

        when(subcategoryRepository.findByCategoryId(1L)).thenReturn(subcategories);
        when(subcategoryMapper.toResponse(any(Subcategory.class))).thenReturn(new SubcategoryResponse());

        // When
        List<SubcategoryResponse> result = subcategoryService.getSubcategoriesByCategoryId(1L);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void updateSubcategory_shouldUpdateSubcategorySuccessfully() {
        // Given
        SubcategoryRequest request = SubcategoryRequest.builder()
                .name("Updated Subcategory")
                .categoryId(2L)
                .build();

        Subcategory existingSubcategory = Subcategory.builder().id(1L).name("Old").build();
        Category newCategory = Category.builder().id(2L).build();
        SubcategoryResponse response = SubcategoryResponse.builder().id(1L).name("Updated Subcategory").build();

        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(existingSubcategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(subcategoryRepository.existsByNameAndCategoryIdAndIdNot(anyString(), anyLong(), anyLong())).thenReturn(false);
        when(subcategoryRepository.save(any(Subcategory.class))).thenReturn(existingSubcategory);
        when(subcategoryMapper.toResponse(any(Subcategory.class))).thenReturn(response);

        // When
        SubcategoryResponse result = subcategoryService.updateSubcategory(1L, request);

        // Then
        assertThat(result).isEqualTo(response);
        verify(subcategoryRepository).save(existingSubcategory);
    }

    @Test
    void deleteSubcategory_shouldDeleteSubcategory() {
        // Given
        when(subcategoryRepository.existsById(1L)).thenReturn(true);

        // When
        subcategoryService.deleteSubcategory(1L);

        // Then
        verify(subcategoryRepository).deleteById(1L);
    }

    @Test
    void deleteSubcategory_shouldThrowNotFoundExceptionWhenNotFound() {
        // Given
        when(subcategoryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> subcategoryService.deleteSubcategory(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Подкатегория не найдена");
    }
}