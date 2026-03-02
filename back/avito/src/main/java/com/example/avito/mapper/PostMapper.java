package com.example.avito.mapper;

import com.example.avito.dto.request.CreatePostRequest;
import com.example.avito.dto.request.UpdatePostRequest;
import com.example.avito.dto.response.PostResponse;
import com.example.avito.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {
    // @Mapping(target = "id", ignore = true)
    // @Mapping(target = "createdAt", ignore = true)
    // @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(CreatePostRequest request);

    // @Mapping(target = "id", ignore = true)
    // @Mapping(target = "createdAt", ignore = true)
    // @Mapping(target = "updatedAt", ignore = true)
    void updatePostFromRequest(UpdatePostRequest request, @MappingTarget Post post);

    PostResponse toResponse(Post post);
}