package com.example.avito.mapper;

import com.example.avito.dto.request.CreatePostRequest;
import com.example.avito.dto.request.UpdatePostRequest;
import com.example.avito.dto.response.PostResponse;
import com.example.avito.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {

    PostResponse toResponse(Post post);
}