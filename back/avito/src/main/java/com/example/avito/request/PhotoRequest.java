package com.example.avito.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoRequest {
    @NotBlank(message = "URL фото обязателен")
    private String url;

    private Boolean primary = false;

    @NotNull(message = "ID объявления обязательно")
    private Long postId;
}