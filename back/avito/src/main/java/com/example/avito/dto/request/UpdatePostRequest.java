package com.example.avito.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePostRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String location;
}