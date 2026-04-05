package com.example.avito.mapper;

import com.example.avito.entity.Purchase;
import com.example.avito.response.PurchaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseMapper {

    private final PostMapper postMapper;

    public PurchaseResponse toResponse(Purchase purchase) {
        return PurchaseResponse.builder()
                .id(purchase.getId())
                .post(postMapper.toResponse(purchase.getPost()))
                .purchasePrice(purchase.getPurchasePrice())
                .createdAt(purchase.getCreatedAt())
                .build();
    }
}