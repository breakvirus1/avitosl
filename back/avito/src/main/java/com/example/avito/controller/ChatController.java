package com.example.avito.controller;

import com.example.avito.entity.ChatMessage;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            Principal principal,
            @Payload String message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(message);
        
        String username = "anonymous";
        if (principal != null) {
            username = principal.getName();
        }
        chatMessage.setSender(username);
        
        return chatMessage;
    }
}