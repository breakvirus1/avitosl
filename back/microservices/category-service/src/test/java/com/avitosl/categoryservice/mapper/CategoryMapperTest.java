package com.avitosl.categoryservice.mapper;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.request.CategoryRequest;
import com.avitosl.categoryservice.response.CategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryMapperImplTest {

    private CategoryMapper categoryMapper;

    @BeforeEach
    void setUp() throws Exception {
        categoryMapper = Mappers.getMapper(CategoryMapper.class);
        // Manually inject SubcategoryMapper dependency since Mappers.getMapper doesn't auto-inject
        SubcategoryMapper subMapper = Mappers.getMapper(SubcategoryMapper.class);
        Field field = categoryMapper.getClass().getDeclaredField("subcategoryMapper");
        field.setAccessible(true);
        field.set(categoryMapper, subMapper);
    }

    @Test
    void toEntity_ShouldConvertCategoryRequestToEntity() {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        request.setDescription("Electronic devices");

        // When
        Category entity = categoryMapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Electronics");
        assertThat(entity.getDescription()).isEqualTo("Electronic devices");
    }

    @Test
    void toResponse_ShouldConvertCategoryToResponseWithSubcategories() {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setDescription("Electronic devices");
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        Subcategory sub1 = new Subcategory();
        sub1.setId(10L);
        sub1.setName("Phones");
        sub1.setCategory(category);
        category.setSubcategories(new HashSet<>(Arrays.asList(sub1)));

        // When
        CategoryResponse response = categoryMapper.toResponse(category);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getSubcategories()).hasSize(1);
        assertThat(response.getSubcategories().get(0).getName()).isEqualTo("Phones");
    }

    @Test
    void toResponseList_ShouldConvertListOfCategories() {
        // Given
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Electronics");

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Clothing");

        // When
        java.util.List<CategoryResponse> responses = categoryMapper.toResponseList(Arrays.asList(cat1, cat2));

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Electronics");
        assertThat(responses.get(1).getName()).isEqualTo("Clothing");
    }

    @Test
    void updateEntityFromRequest_ShouldUpdateEntityFields() {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Old Name");
        category.setDescription("Old description");

        CategoryRequest request = new CategoryRequest();
        request.setName("New Name");
        request.setDescription("New description");

        // When
        categoryMapper.updateEntityFromRequest(request, category);

        // Then
        assertThat(category.getName()).isEqualTo("New Name");
        assertThat(category.getDescription()).isEqualTo("New description");
    }
}
