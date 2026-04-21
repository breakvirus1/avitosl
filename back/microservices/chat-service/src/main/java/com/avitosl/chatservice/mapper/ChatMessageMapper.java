package com.avitosl.chatservice.mapper;

import com.avitosl.chatservice.entity.ChatMessage;
import com.avitosl.chatservice.request.ChatMessageRequest;
import com.avitosl.chatservice.response.ChatMessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    ChatMessage toEntity(ChatMessageRequest request);

    ChatMessageResponse toResponse(ChatMessage chatMessage);

    List<ChatMessageResponse> toResponseList(List<ChatMessage> chatMessages);

    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(ChatMessageRequest request, @MappingTarget ChatMessage chatMessage);
}
