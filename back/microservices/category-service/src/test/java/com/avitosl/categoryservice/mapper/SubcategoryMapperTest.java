package com.avitosl.categoryservice.mapper;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.request.SubcategoryRequest;
import com.avitosl.categoryservice.response.SubcategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SubcategoryMapperImplTest {

    private SubcategoryMapper subcategoryMapper;

    @BeforeEach
    void setUp() {
        subcategoryMapper = Mappers.getMapper(SubcategoryMapper.class);
    }

    @Test
    void toEntity_ShouldConvertRequestToEntity() {
        // Given
        SubcategoryRequest request = new SubcategoryRequest();
        request.setName("Smartphones");
        request.setCategoryId(1L);

        // When
        Subcategory entity = subcategoryMapper.toEntity(request);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getName()).isEqualTo("Smartphones");
        assertThat(entity.getCategory()).isNull(); // category will be set separately
    }

    @Test
    void toResponse_ShouldConvertEntityToResponse() {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Subcategory subcategory = new Subcategory();
        subcategory.setId(10L);
        subcategory.setName("Smartphones");
        subcategory.setCategory(category);
        subcategory.setCreatedAt(LocalDateTime.now());
        subcategory.setUpdatedAt(LocalDateTime.now());

        // When
        SubcategoryResponse response = subcategoryMapper.toResponse(subcategory);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("Smartphones");
        assertThat(response.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void updateEntityFromRequest_ShouldUpdateEntity() {
        // Given
        Subcategory subcategory = new Subcategory();
        subcategory.setId(10L);
        subcategory.setName("Old Name");

        SubcategoryRequest request = new SubcategoryRequest();
        request.setName("Smartphones");
        request.setCategoryId(1L);

        // When
        subcategoryMapper.updateEntityFromRequest(request, subcategory);

        // Then
        assertThat(subcategory.getName()).isEqualTo("Smartphones");
        // Note: categoryId won't be set directly on entity - would need to fetch Category
    }
}
