package com.avitosl.categoryservice.mapper;

import com.avitosl.categoryservice.entity.Category;
import com.avitosl.categoryservice.request.CategoryRequest;
import com.avitosl.categoryservice.response.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "subcategories", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
