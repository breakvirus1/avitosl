package com.avitosl.userservice.mapper;

import com.avitosl.userservice.entity.User;
import com.avitosl.userservice.request.RegisterRequest;
import com.avitosl.userservice.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);

    List<UserResponse> toResponseList(List<User> users);

    void updateEntityFromRequest(RegisterRequest request, @MappingTarget User user);
}
