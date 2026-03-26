package com.example.avito.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
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

    @DecimalMin(value = "0.0", inclusive = true, message = "Цена не может быть отрицательной")
    @DecimalMax(value = "99999999.99", inclusive = true, message = "Цена не может превышать 99,999,999.99")
    private Double price;

    @NotNull(message = "ID категории обязателен")
    private Long categoryId;

    @NotNull(message = "ID подкатегории обязателен")
    private Long subcategoryId;

    private List<String> photoUrls;
}
