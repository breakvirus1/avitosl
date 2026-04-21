package com.avitosl.categoryservice.controller;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.request.CategoryRequest;
import com.avitosl.categoryservice.response.CategoryResponse;
import com.avitosl.categoryservice.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @Test
    void createCategory_ShouldReturnCategoryResponse() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Electronic devices");

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic devices");

        when(categoryService.createCategory(any(Category.class))).thenReturn(category);

        // When/Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryById_ShouldReturnCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        when(categoryService.getCategoryById(1L)).thenReturn(category);

        // When/Then
        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryByName_ShouldReturnCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setName("Electronics");
        when(categoryService.getCategoryByName("Electronics")).thenReturn(category);

        // When/Then
        mockMvc.perform(get("/api/categories/name/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        // Given
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Cat1");
        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Cat2");
        when(categoryService.getAllCategories()).thenReturn(Arrays.asList(cat1, cat2));

        // When/Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cat1"))
                .andExpect(jsonPath("$[1].name").value("Cat2"));
    }

    @Test
    void updateCategory_ShouldUpdateAndReturn() throws Exception {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");
        request.setDescription("Updated desc");

        Category existing = new Category();
        existing.setId(1L);
        existing.setName("Old");
        existing.setDescription("Old desc");

        Category updated = new Category();
        updated.setId(1L);
        updated.setName("Updated");
        updated.setDescription("Updated desc");

        when(categoryService.updateCategory(eq(1L), any(Category.class))).thenReturn(updated);

        // When/Then
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteCategory_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When/Then
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }
}
