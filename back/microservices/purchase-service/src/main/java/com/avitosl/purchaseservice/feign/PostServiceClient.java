package com.avitosl.purchaseservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.avitosl.purchaseservice.response.PostResponse;

@FeignClient(name = "post-service", path = "/api/posts",
             configuration = FeignConfig.class,
             fallback = PostServiceClientFallback.class)
public interface PostServiceClient {

    @GetMapping("/{id}")
    PostResponse getPostById(@PathVariable Long id);

    @PostMapping("/{id}/deactivate")
    void deactivatePost(@PathVariable Long id);
}