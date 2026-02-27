package com.example.avito.controller;

import com.example.avito.entity.Image;
import com.example.avito.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@Tag(name = "Image Controller", description = "поерации с картинками в обьявах")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @Operation(summary = "добавление картинки к посту")
    @PostMapping
    public Image createImage(@RequestBody Image image) {
        return imageService.createImage(image);
    }

    @Operation(summary = "поиск изображений по id обьявы")
    @GetMapping("/post/{postId}")
    public List<Image> findByPostId(@PathVariable Long postId) {
        return imageService.findByPostId(postId);
    }

    @Operation(summary = "удалить изображение по id")
    @DeleteMapping("/{id}")
    public void deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
    }
}