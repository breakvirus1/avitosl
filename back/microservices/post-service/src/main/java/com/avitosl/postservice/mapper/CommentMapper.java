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
    @Mapping(target = "userId", source = "userId")
    Comment toEntity(CommentRequest request);

    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);

    void updateEntityFromRequest(CommentRequest request, @MappingTarget Comment comment);
}
