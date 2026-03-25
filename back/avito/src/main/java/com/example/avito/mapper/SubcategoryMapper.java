package com.example.avito.mapper;

import com.example.avito.entity.Category;
import com.example.avito.entity.Subcategory;
import com.example.avito.request.SubcategoryRequest;
import com.example.avito.response.SubcategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubcategoryMapper {
    SubcategoryMapper INSTANCE = Mappers.getMapper(SubcategoryMapper.class);
    
    default SubcategoryResponse toResponse(Subcategory subcategory) {
        if (subcategory == null) {
            return null;
        }
        Category category = subcategory.getCategory();
        return SubcategoryResponse.builder()
                .id(subcategory.getId())
                .name(subcategory.getName())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .createdAt(subcategory.getCreatedAt())
                .updatedAt(subcategory.getUpdatedAt())
                .build();
    }
    
    default Subcategory toEntity(SubcategoryRequest request) {
        if (request == null) {
            return null;
        }
        Subcategory subcategory = new Subcategory();
        subcategory.setName(request.getName());
        // category будет установлен в сервисе
        return subcategory;
    }
    
    default List<SubcategoryResponse> toResponseList(List<Subcategory> subcategories) {
        if (subcategories == null) {
            return null;
        }
        return subcategories.stream()
                .map(this::toResponse)
                .toList();
    }
}
