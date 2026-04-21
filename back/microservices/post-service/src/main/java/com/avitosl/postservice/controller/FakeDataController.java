package com.avitosl.postservice.controller;

import com.avitosl.postservice.entity.Post;
import com.avitosl.postservice.service.FakeDataGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fake-data")
@RequiredArgsConstructor
public class FakeDataController {

    private final FakeDataGeneratorService fakeDataGeneratorService;

    @PostMapping("/posts/{count}")
    public ResponseEntity<List<Post>> generateFakePosts(@PathVariable int count) {
        List<Post> posts = fakeDataGeneratorService.generateFakePosts(count);
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/posts")
    public ResponseEntity<Void> clearAllPosts() {
        fakeDataGeneratorService.clearAllPosts();
        return ResponseEntity.ok().build();
    }
}