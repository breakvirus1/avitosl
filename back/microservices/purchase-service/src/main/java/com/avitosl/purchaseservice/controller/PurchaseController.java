package com.avitosl.purchaseservice.controller;

import com.avitosl.purchaseservice.entity.Purchase;
import com.avitosl.purchaseservice.request.PurchaseRequest;
import com.avitosl.purchaseservice.response.PurchaseResponse;
import com.avitosl.purchaseservice.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        Purchase purchase = purchaseService.createPurchase(
                new Purchase(null, request.getBuyerId(), request.getSellerId(), request.getPostId(),
                        request.getAmount(), "PENDING", request.getNotes(), null, null)
        );
        return ResponseEntity.ok(mapToResponse(purchase));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        Purchase purchase = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(mapToResponse(purchase));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByBuyerId(@PathVariable Long buyerId) {
        List<Purchase> purchases = purchaseService.getPurchasesByBuyerId(buyerId);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesBySellerId(@PathVariable Long sellerId) {
        List<Purchase> purchases = purchaseService.getPurchasesBySellerId(sellerId);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByPostId(@PathVariable Long postId) {
        List<Purchase> purchases = purchaseService.getPurchasesByPostId(postId);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByStatus(@PathVariable String status) {
        List<Purchase> purchases = purchaseService.getPurchasesByStatus(status);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PurchaseResponse> updatePurchase(@PathVariable Long id,
                                                            @Valid @RequestBody PurchaseRequest request) {
        Purchase purchaseDetails = new Purchase(null, request.getBuyerId(), request.getSellerId(),
                request.getPostId(), request.getAmount(), request.getStatus(), request.getNotes(), null, null);
        Purchase updatedPurchase = purchaseService.updatePurchase(id, purchaseDetails);
        return ResponseEntity.ok(mapToResponse(updatedPurchase));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PurchaseResponse> updatePurchaseStatus(@PathVariable Long id,
                                                                 @RequestParam String status) {
        Purchase updatedPurchase = purchaseService.updatePurchaseStatus(id, status);
        return ResponseEntity.ok(mapToResponse(updatedPurchase));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePurchase(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
        return ResponseEntity.noContent().build();
    }

    private PurchaseResponse mapToResponse(Purchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getBuyerId(),
                purchase.getSellerId(),
                purchase.getPostId(),
                purchase.getAmount(),
                purchase.getStatus(),
                purchase.getNotes(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt()
        );
    }
}
