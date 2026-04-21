package com.avitosl.purchaseservice.mapper;

import com.avitosl.purchaseservice.entity.Purchase;
import com.avitosl.purchaseservice.request.PurchaseRequest;
import com.avitosl.purchaseservice.response.PurchaseResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseMapper {

    Purchase toEntity(PurchaseRequest request);

    PurchaseResponse toResponse(Purchase purchase);

    List<PurchaseResponse> toResponseList(List<Purchase> purchases);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PurchaseRequest request, @MappingTarget Purchase purchase);
}
