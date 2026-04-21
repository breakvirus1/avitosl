package com.avitosl.categoryservice.service;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.exception.ConflictException;
import com.avitosl.categoryservice.exception.NotFoundException;
import com.avitosl.categoryservice.mapper.CategoryMapper;
import com.avitosl.categoryservice.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Captor
    private ArgumentCaptor<Long> idCaptor;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");
        category1.setDescription("Electronic devices and gadgets");
        category1.setCreatedAt(LocalDateTime.now());
        category1.setUpdatedAt(LocalDateTime.now());

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Clothing");
        category2.setDescription("Fashion and apparel");
        category2.setCreatedAt(LocalDateTime.now());
        category2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createCategory_ShouldSaveCategory_WhenNameIsUnique() {
        // Given
        when(categoryRepository.existsByName("Electronics")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            cat.setId(1L);
            cat.setCreatedAt(LocalDateTime.now());
            cat.setUpdatedAt(LocalDateTime.now());
            return cat;
        });

        Category newCategory = new Category();
        newCategory.setName("Electronics");
        newCategory.setDescription("Electronic devices");

        // When
        Category result = categoryService.createCategory(newCategory);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertEquals("Electronic devices", result.getDescription());
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ShouldThrowConflictException_WhenNameExists() {
        // Given
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        Category category = new Category();
        category.setName("Electronics");

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            categoryService.createCategory(category);
        });

        assertThat(exception.getMessage()).contains("already exists");
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_ShouldReturnCategory_WhenExists() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        // When
        Category result = categoryService.getCategoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            categoryService.getCategoryById(999L);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(categoryRepository).findById(999L);
    }

    @Test
    void getCategoryByName_ShouldReturnCategory_WhenExists() {
        // Given
        when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(category1));

        // When
        Category result = categoryService.getCategoryByName("Electronics");

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        verify(categoryRepository).findByName("Electronics");
    }

    @Test
    void getCategoryByName_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(categoryRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            categoryService.getCategoryByName("Nonexistent");
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(categoryRepository).findByName("Nonexistent");
    }

    @Test
    void getAllCategories_ShouldReturnAllCategories() {
        // Given
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        // When
        List<Category> result = categoryService.getAllCategories();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(category1));
        assertTrue(result.contains(category2));
        verify(categoryRepository).findAll();
    }

    @Test
    void updateCategory_ShouldUpdateCategory_WhenNameIsUnchanged() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updateDetails = new Category();
        updateDetails.setName("Electronics");
        updateDetails.setDescription("Updated description");

        // When
        Category result = categoryService.updateCategory(1L, updateDetails);

        // Then
        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertEquals("Updated description", result.getDescription());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldUpdateCategoryAndCheckNameConflict_WhenNameChanges() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByName("Clothing")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Category updateDetails = new Category();
        updateDetails.setName("Clothing");
        updateDetails.setDescription("Updated description");

        // When
        Category result = categoryService.updateCategory(1L, updateDetails);

        // Then
        assertNotNull(result);
        assertEquals("Clothing", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Clothing");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowConflictException_WhenNewNameAlreadyExists() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(categoryRepository.existsByName("Clothing")).thenReturn(true);

        Category updateDetails = new Category();
        updateDetails.setName("Clothing");

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            categoryService.updateCategory(1L, updateDetails);
        });

        assertThat(exception.getMessage()).contains("already exists");
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).existsByName("Clothing");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void updateCategory_ShouldThrowNotFoundException_WhenCategoryDoesNotExist() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Category updateDetails = new Category();
        updateDetails.setName("NewName");

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            categoryService.updateCategory(999L, updateDetails);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_ShouldDeleteCategory_WhenExists() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        doNothing().when(categoryRepository).delete(any(Category.class));

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category1);
    }

    @Test
    void deleteCategory_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            categoryService.deleteCategory(999L);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
