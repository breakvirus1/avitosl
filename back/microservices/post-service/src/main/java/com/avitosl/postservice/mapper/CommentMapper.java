package com.avitosl.postservice.mapper;

import com.avitosl.postservice.entity.Comment;
import com.avitosl.postservice.request.CommentRequest;
import com.avitosl.postservice.response.CommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userId", ignore = true) // userId определяется через keycloakId
    Comment toEntity(CommentRequest request);

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "authorFirstName", ignore = true)
    @Mapping(target = "authorLastName", ignore = true)
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);

    @Mapping(target = "post", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateEntityFromRequest(CommentRequest request, @MappingTarget Comment comment);
}
