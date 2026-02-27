package com.example.avito.mapper;

import com.example.avito.dto.response.UserResponse;
import com.example.avito.entity.User;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}