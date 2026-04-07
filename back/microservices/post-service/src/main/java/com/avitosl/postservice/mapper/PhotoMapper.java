package com.avitosl.postservice.mapper;

import com.avitosl.postservice.entity.Photo;
import com.avitosl.postservice.request.PhotoRequest;
import com.avitosl.postservice.response.PhotoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhotoMapper {

    @Mapping(target = "post", ignore = true)
    Photo toEntity(PhotoRequest request);

    PhotoResponse toResponse(Photo photo);

    List<PhotoResponse> toResponseList(List<Photo> photos);

    void updateEntityFromRequest(PhotoRequest request, @MappingTarget Photo photo);
}
