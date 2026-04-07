package com.avitosl.categoryservice.mapper;

import com.avitosl.categoryservice.entity.Subcategory;
import com.avitosl.categoryservice.request.SubcategoryRequest;
import com.avitosl.categoryservice.response.SubcategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubcategoryMapper {

    @Mapping(target = "category", ignore = true)
    Subcategory toEntity(SubcategoryRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    SubcategoryResponse toResponse(Subcategory subcategory);

    List<SubcategoryResponse> toResponseList(List<Subcategory> subcategories);

    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(SubcategoryRequest request, @MappingTarget Subcategory subcategory);
}
