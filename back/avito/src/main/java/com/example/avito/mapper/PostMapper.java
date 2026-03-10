package com.example.avito.mapper;

import com.example.avito.entity.Post;
import com.example.avito.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class, PhotoMapper.class})
public interface PostMapper {
    PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> posts);
}