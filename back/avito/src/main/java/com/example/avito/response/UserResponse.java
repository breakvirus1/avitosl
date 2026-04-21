package com.example.avito.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String phoneNumber;
    private boolean enabled;
    private BigDecimal walletBalance;
    private List<PurchaseResponse> purchases;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}