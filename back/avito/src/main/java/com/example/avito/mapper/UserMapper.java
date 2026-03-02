// src/main/java/com/example/avito/mapper/UserMapper.java
package com.example.avito.mapper;

import com.example.avito.dto.response.*;
import com.example.avito.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    


 
    User toResponse(User user);
}