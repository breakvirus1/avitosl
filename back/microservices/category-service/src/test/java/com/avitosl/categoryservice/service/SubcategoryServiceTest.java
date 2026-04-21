package com.avitosl.categoryservice.service;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.exception.ConflictException;
import com.avitosl.categoryservice.exception.NotFoundException;
import com.avitosl.categoryservice.mapper.SubcategoryMapper;
import com.avitosl.categoryservice.repository.CategoryRepository;
import com.avitosl.categoryservice.repository.SubcategoryRepository;
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
class SubcategoryServiceTest {

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryMapper subcategoryMapper;

    @InjectMocks
    private SubcategoryService subcategoryService;

    private Category category;
    private Subcategory subcategory1;
    private Subcategory subcategory2;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic devices");

        subcategory1 = new Subcategory();
        subcategory1.setId(1L);
        subcategory1.setName("Smartphones");
        subcategory1.setDescription("Mobile phones");
        subcategory1.setCategory(category);
        subcategory1.setCreatedAt(LocalDateTime.now());
        subcategory1.setUpdatedAt(LocalDateTime.now());

        subcategory2 = new Subcategory();
        subcategory2.setId(2L);
        subcategory2.setName("Laptops");
        subcategory2.setDescription("Portable computers");
        subcategory2.setCategory(category);
        subcategory2.setCreatedAt(LocalDateTime.now());
        subcategory2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createSubcategory_ShouldSaveSubcategory_WhenCategoryExistsAndNameIsUnique() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryId("Smartphones", 1L)).thenReturn(false);
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(invocation -> {
            Subcategory sub = invocation.getArgument(0);
            sub.setId(1L);
            sub.setCreatedAt(LocalDateTime.now());
            sub.setUpdatedAt(LocalDateTime.now());
            return sub;
        });

        Subcategory newSubcategory = new Subcategory();
        newSubcategory.setName("Smartphones");
        newSubcategory.setDescription("Mobile phones");
        Category cat = new Category();
        cat.setId(1L);
        newSubcategory.setCategory(cat);

        // When
        Subcategory result = subcategoryService.createSubcategory(newSubcategory);

        // Then
        assertNotNull(result);
        assertEquals("Smartphones", result.getName());
        assertEquals("Electronics", result.getCategory().getName());
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByNameAndCategoryId("Smartphones", 1L);
        verify(subcategoryRepository).save(any(Subcategory.class));
    }

    @Test
    void createSubcategory_ShouldThrowNotFoundException_WhenCategoryDoesNotExist() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Subcategory subcategory = new Subcategory();
        subcategory.setName("Test");
        Category cat = new Category();
        cat.setId(999L);
        subcategory.setCategory(cat);

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            subcategoryService.createSubcategory(subcategory);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(categoryRepository).findById(999L);
        verify(subcategoryRepository, never()).save(any(Subcategory.class));
    }

    @Test
    void createSubcategory_ShouldThrowConflictException_WhenNameExistsInCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryId("Smartphones", 1L)).thenReturn(true);

        Subcategory subcategory = new Subcategory();
        subcategory.setName("Smartphones");
        Category cat = new Category();
        cat.setId(1L);
        subcategory.setCategory(cat);

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            subcategoryService.createSubcategory(subcategory);
        });

        assertThat(exception.getMessage()).contains("already exists");
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByNameAndCategoryId("Smartphones", 1L);
        verify(subcategoryRepository, never()).save(any(Subcategory.class));
    }

    @Test
    void getSubcategoryById_ShouldReturnSubcategory_WhenExists() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory1));

        // When
        Subcategory result = subcategoryService.getSubcategoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Smartphones", result.getName());
        verify(subcategoryRepository).findById(1L);
    }

    @Test
    void getSubcategoryById_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(subcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            subcategoryService.getSubcategoryById(999L);
        });

        assertThat(exception.getMessage()).contains("Subcategory not found");
        verify(subcategoryRepository).findById(999L);
    }

    @Test
    void getSubcategoriesByCategoryId_ShouldReturnList_WhenCategoryExists() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.findByCategoryId(1L)).thenReturn(Arrays.asList(subcategory1, subcategory2));

        // When
        List<Subcategory> result = subcategoryService.getSubcategoriesByCategoryId(1L);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(subcategory1));
        assertTrue(result.contains(subcategory2));
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).findByCategoryId(1L);
    }

    @Test
    void getSubcategoriesByCategoryId_ShouldThrowNotFoundException_WhenCategoryNotExists() {
        // Given
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            subcategoryService.getSubcategoriesByCategoryId(999L);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(categoryRepository).findById(999L);
        verify(subcategoryRepository, never()).findByCategoryId(any());
    }

    @Test
    void getAllSubcategories_ShouldReturnAll() {
        // Given
        when(subcategoryRepository.findAll()).thenReturn(Arrays.asList(subcategory1, subcategory2));

        // When
        List<Subcategory> result = subcategoryService.getAllSubcategories();

        // Then
        assertEquals(2, result.size());
        verify(subcategoryRepository).findAll();
    }

    @Test
    void updateSubcategory_ShouldUpdate_WhenNameIsUnchanged() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory1));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.save(any(Subcategory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Subcategory updateDetails = new Subcategory();
        updateDetails.setName("Smartphones");
        updateDetails.setDescription("Updated description");
        Category cat = new Category();
        cat.setId(1L);
        updateDetails.setCategory(cat);

        // When
        Subcategory result = subcategoryService.updateSubcategory(1L, updateDetails);

        // Then
        assertNotNull(result);
        assertEquals("Smartphones", result.getName());
        assertEquals("Updated description", result.getDescription());
        verify(subcategoryRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).save(any(Subcategory.class));
    }

    @Test
    void updateSubcategory_ShouldThrowConflictException_WhenNameChangedToExisting() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory1));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(subcategoryRepository.existsByNameAndCategoryId("Laptops", 1L)).thenReturn(true);

        Subcategory updateDetails = new Subcategory();
        updateDetails.setName("Laptops");
        Category cat = new Category();
        cat.setId(1L);
        updateDetails.setCategory(cat);

        // When/Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            subcategoryService.updateSubcategory(1L, updateDetails);
        });

        assertThat(exception.getMessage()).contains("already exists");
        verify(subcategoryRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByNameAndCategoryId("Laptops", 1L);
        verify(subcategoryRepository, never()).save(any(Subcategory.class));
    }

    @Test
    void updateSubcategory_ShouldThrowNotFoundException_WhenSubcategoryDoesNotExist() {
        // Given
        when(subcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        Subcategory updateDetails = new Subcategory();
        updateDetails.setName("NewName");
        Category cat = new Category();
        cat.setId(1L);
        updateDetails.setCategory(cat);

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            subcategoryService.updateSubcategory(999L, updateDetails);
        });

        assertThat(exception.getMessage()).contains("Subcategory not found");
        verify(subcategoryRepository).findById(999L);
        verify(subcategoryRepository, never()).save(any(Subcategory.class));
    }

    @Test
    void updateSubcategory_ShouldThrowNotFoundException_WhenCategoryDoesNotExist() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory1));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Subcategory updateDetails = new Subcategory();
        updateDetails.setName("NewName");
        Category cat = new Category();
        cat.setId(999L);
        updateDetails.setCategory(cat);

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            subcategoryService.updateSubcategory(1L, updateDetails);
        });

        assertThat(exception.getMessage()).contains("Category not found");
        verify(subcategoryRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(subcategoryRepository, never()).save(any(Subcategory.class));
    }

    @Test
    void deleteSubcategory_ShouldDelete_WhenExists() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(subcategory1));
        doNothing().when(subcategoryRepository).delete(any(Subcategory.class));

        // When
        subcategoryService.deleteSubcategory(1L);

        // Then
        verify(subcategoryRepository).findById(1L);
        verify(subcategoryRepository).delete(subcategory1);
    }

    @Test
    void deleteSubcategory_ShouldThrowNotFoundException_WhenNotExists() {
        // Given
        when(subcategoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            subcategoryService.deleteSubcategory(999L);
        });

        assertThat(exception.getMessage()).contains("Subcategory not found");
        verify(subcategoryRepository).findById(999L);
        verify(subcategoryRepository, never()).delete(any(Subcategory.class));
    }
}
