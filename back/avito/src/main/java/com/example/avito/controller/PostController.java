package com.example.avito.controller;

import com.example.avito.dto.response.PostResponse;
import com.example.avito.entity.Post;
import com.example.avito.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post Controller", description = "операции с объявлениями")
public class PostController {
    @Autowired
    private PostService postService;

    @Operation(summary = "создание нового объявления")
    @PostMapping
    public PostResponse createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    @Operation(summary = "получение всех объявлений")
    @GetMapping
    public List<PostResponse> findAllPosts() {
        return postService.findAllPosts();
    }

    @Operation(summary = "поиск обьявлений по категориям")
    @GetMapping("/category/{category}")
    public List<PostResponse> findByCategory(@PathVariable String category) {
        return postService.findByCategory(category);
    }

  

    @Operation(summary = "обновить обьявление")
    @PutMapping("/{id}")
    public PostResponse updatePost(@PathVariable Long id, @RequestBody Post post) {
        return postService.updatePost(id, post);
    }

    @Operation(summary = "удалить обьявление")
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deletePost(id);
    }
}