package com.avitosl.postservice.response;

import com.avitosl.postservice.entity.Photo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private String keycloakId;
    private UserResponse author;
    private Long categoryId;
    private Long subcategoryId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<Photo> photos = new HashSet<>();
}
