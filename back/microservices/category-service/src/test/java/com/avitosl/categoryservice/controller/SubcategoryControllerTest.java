package com.avitosl.categoryservice.controller;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.request.SubcategoryRequest;
import com.avitosl.categoryservice.response.SubcategoryResponse;
import com.avitosl.categoryservice.service.SubcategoryService;
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
@WebMvcTest(SubcategoryController.class)
class SubcategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubcategoryService subcategoryService;

    @Test
    void createSubcategory_ShouldReturnSubcategoryResponse() throws Exception {
        // Given
        SubcategoryRequest request = new SubcategoryRequest();
        request.setName("Smartphones");
        request.setDescription("Mobile phones");
        request.setCategoryId(1L);

        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Smartphones");
        subcategory.setDescription("Mobile phones");
        Category cat = new Category();
        cat.setId(1L);
        subcategory.setCategory(cat);
        subcategory.setCreatedAt(LocalDateTime.now());
        subcategory.setUpdatedAt(LocalDateTime.now());

        when(subcategoryService.createSubcategory(any(Subcategory.class))).thenReturn(subcategory);

        // When/Then
        mockMvc.perform(post("/api/subcategories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphones"));
    }

    @Test
    void getSubcategoryById_ShouldReturnSubcategory() throws Exception {
        // Given
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Smartphones");
        Category cat = new Category();
        cat.setId(1L);
        subcategory.setCategory(cat);
        when(subcategoryService.getSubcategoryById(1L)).thenReturn(subcategory);

        // When/Then
        mockMvc.perform(get("/api/subcategories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphones"));
    }

    @Test
    void getSubcategoriesByCategoryId_ShouldReturnList() throws Exception {
        // Given
        Subcategory sub1 = new Subcategory();
        sub1.setId(1L);
        sub1.setName("Sub1");
        Category cat = new Category();
        cat.setId(1L);
        sub1.setCategory(cat);

        Subcategory sub2 = new Subcategory();
        sub2.setId(2L);
        sub2.setName("Sub2");
        sub2.setCategory(cat);

        when(subcategoryService.getSubcategoriesByCategoryId(1L)).thenReturn(Arrays.asList(sub1, sub2));

        // When/Then
        mockMvc.perform(get("/api/subcategories/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sub1"))
                .andExpect(jsonPath("$[1].name").value("Sub2"));
    }

    @Test
    void getAllSubcategories_ShouldReturnList() throws Exception {
        // Given
        Subcategory sub1 = new Subcategory();
        sub1.setId(1L);
        sub1.setName("Sub1");
        Category cat = new Category();
        cat.setId(1L);
        sub1.setCategory(cat);

        Subcategory sub2 = new Subcategory();
        sub2.setId(2L);
        sub2.setName("Sub2");
        sub2.setCategory(cat);

        when(subcategoryService.getAllSubcategories()).thenReturn(Arrays.asList(sub1, sub2));

        // When/Then
        mockMvc.perform(get("/api/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sub1"));
    }

    @Test
    void updateSubcategory_ShouldUpdateAndReturn() throws Exception {
        // Given
        SubcategoryRequest request = new SubcategoryRequest();
        request.setName("Updated");
        request.setDescription("Updated desc");
        request.setCategoryId(1L);

        Subcategory updated = new Subcategory();
        updated.setId(1L);
        updated.setName("Updated");
        updated.setDescription("Updated desc");
        Category cat = new Category();
        cat.setId(1L);
        updated.setCategory(cat);

        when(subcategoryService.updateSubcategory(eq(1L), any(Subcategory.class))).thenReturn(updated);

        // When/Then
        mockMvc.perform(put("/api/subcategories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteSubcategory_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(subcategoryService).deleteSubcategory(1L);

        // When/Then
        mockMvc.perform(delete("/api/subcategories/1"))
                .andExpect(status().isNoContent());
    }
}
