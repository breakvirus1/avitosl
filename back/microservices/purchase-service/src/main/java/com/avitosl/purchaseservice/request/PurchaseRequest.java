package com.avitosl.purchaseservice.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PurchaseRequest {
    @NotNull
    private Long buyerId;

    @NotNull
    private Long sellerId;

    @NotNull
    private Long postId;

    @NotNull
    @Positive
    private Double amount;

    private String status;

    private String notes;
}
