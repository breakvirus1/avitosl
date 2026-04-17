package com.avitosl.postservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.avitosl.postservice.request.ChatMessageRequest;
import com.avitosl.postservice.response.ChatMessageResponse;

@FeignClient(name = "chat-service", path = "/api/chat",
             configuration = FeignConfig.class)
public interface ChatServiceClient {

    @PostMapping("/messages")
    ChatMessageResponse sendMessage(@RequestHeader("Authorization") String authorization,
                                    @RequestBody ChatMessageRequest request);
}
