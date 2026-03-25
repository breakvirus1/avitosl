package com.example.avito.mapper;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import com.example.avito.response.CategoryResponse;
import com.example.avito.response.SubcategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = SubcategoryMapper.class)
public interface CategoryMapper {
    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(source = "subcategories", target = "subcategories")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);
}