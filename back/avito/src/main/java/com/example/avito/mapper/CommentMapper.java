package com.example.avito.mapper;

import com.example.avito.dto.request.CreateCommentRequest;
import com.example.avito.dto.response.CommentResponse;
import com.example.avito.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, PostMapper.class})
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Comment toEntity(CreateCommentRequest request);

    CommentResponse toResponse(Comment comment);
}