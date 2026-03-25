package com.example.avito.mapper;

import com.example.avito.entity.User;
import com.example.avito.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);
}