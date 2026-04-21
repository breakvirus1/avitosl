package com.avitosl.postservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank
    private String content;

    @NotNull
    private Long postId;

    // keycloakId пользователя из токена (не из тела, но принимаем для совместимости)
    private String keycloakId;
}
