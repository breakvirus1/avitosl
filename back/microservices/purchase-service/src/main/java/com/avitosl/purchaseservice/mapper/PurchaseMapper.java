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

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Purchase toEntity(PurchaseRequest request);

    @Mapping(target = "purchasePrice", source = "amount")
    @Mapping(target = "post", ignore = true)
    PurchaseResponse toResponse(Purchase purchase);

    List<PurchaseResponse> toResponseList(List<Purchase> purchases);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(PurchaseRequest request, @MappingTarget Purchase purchase);
}
