package com.example.avito.mapper;

import com.example.avito.entity.Post;
import com.example.avito.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, SubcategoryMapper.class, PhotoMapper.class})
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    @Mapping(source = "category", target = "category")
    @Mapping(source = "subcategory", target = "subcategory")
    @Mapping(source = "photos", target = "photos")
    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> posts);
}
