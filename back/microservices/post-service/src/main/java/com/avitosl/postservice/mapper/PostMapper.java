package com.avitosl.postservice.mapper;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.request.PostRequest;
import com.avitosl.postservice.response.PostResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE, unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Post toEntity(PostRequest request);

    PostResponse toResponse(Post post);

    List<PostResponse> toResponseList(List<Post> posts);

    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PostRequest request, @MappingTarget Post post);
}
