package com.example.avito.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    @NotBlank(message = "Текст комментария обязателен")
    @Size(min = 1, max = 2000, message = "Комментарий должен содержать от 1 до 2000 символов")
    private String text;

    @NotNull(message = "ID объявления обязательно")
    private Long postId;
}