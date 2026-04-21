package com.avitosl.postservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PostUpdateRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Positive
    private Double price;

    private Long categoryId;

    private Long subcategoryId;

    private Boolean isActive;
}
