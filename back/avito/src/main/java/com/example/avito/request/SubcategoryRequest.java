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
public class SubcategoryRequest {
    
    @NotBlank(message = "Название подкатегории обязательно")
    private String name;
    
    @NotNull(message = "ID категории обязательно")
    private Long categoryId;
}
