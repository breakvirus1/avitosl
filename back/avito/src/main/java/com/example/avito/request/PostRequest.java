package com.example.avito.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequest {
    @NotBlank(message = "Заголовок обязателен")
    @Size(min = 3, max = 200, message = "Заголовок должен содержать от 3 до 200 символов")
    private String title;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    private Double price;

    @NotNull(message = "Список категорий обязателен")
    private List<Long> categoryIds;

    private List<String> photoUrls;
}