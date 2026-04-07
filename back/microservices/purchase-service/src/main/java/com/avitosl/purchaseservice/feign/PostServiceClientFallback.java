package com.avitosl.purchaseservice.feign;

import org.springframework.stereotype.Component;
import com.avitosl.purchaseservice.response.PostResponse;

@Component
public class PostServiceClientFallback implements PostServiceClient {

    @Override
    public PostResponse getPostById(Long id) {
        throw new RuntimeException("Post service is unavailable. Cannot fetch post with id: " + id);
    }
}