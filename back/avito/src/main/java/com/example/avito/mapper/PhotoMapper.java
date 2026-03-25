package com.example.avito.mapper;

import com.example.avito.entity.Photo;
import com.example.avito.response.PhotoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhotoMapper {
    PhotoMapper INSTANCE = Mappers.getMapper(PhotoMapper.class);

    @Mapping(source = "post.id", target = "postId")
    PhotoResponse toResponse(Photo photo);

    List<PhotoResponse> toResponseList(List<Photo> photos);
}