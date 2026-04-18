package com.avitosl.purchaseservice.controller;

import com.avitosl.purchaseservice.entity.Purchase;
import com.avitosl.purchaseservice.request.PurchaseRequest;
import com.avitosl.purchaseservice.response.PostResponse;
import com.avitosl.purchaseservice.response.PurchaseResponse;
import com.avitosl.purchaseservice.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    // Legacy endpoint: create purchase with full request body (requires buyerId/sellerId)
    @PostMapping
    public ResponseEntity<PurchaseResponse> createPurchase(@Valid @RequestBody PurchaseRequest request) {
        Purchase purchase = purchaseService.createPurchase(
                new Purchase(null, request.getBuyerId(), request.getSellerId(), request.getPostId(),
                        request.getAmount(), "PENDING", request.getNotes(), null, null)
        );
        return ResponseEntity.ok(mapToResponse(purchase));
    }

    // Main purchase endpoint used by frontend: POST /api/purchases/posts/{postId}
    @PostMapping("/posts/{postId}")
    public ResponseEntity<PurchaseResponse> purchasePost(@PathVariable Long postId) {
        Long buyerId = getCurrentUserId();
        PurchaseResponse response = purchaseService.purchasePost(postId, buyerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponse> getPurchaseById(@PathVariable Long id) {
        Purchase purchase = purchaseService.getPurchaseById(id);
        return ResponseEntity.ok(mapToResponse(purchase));
    }

    // Frontend uses GET /api/purchases (paginated, for current user)
    @GetMapping
    public ResponseEntity<Page<PurchaseResponse>> getUserPurchases(Pageable pageable) {
        Long buyerId = getCurrentUserId();
        Page<Purchase> purchases = purchaseService.getPurchasesByBuyerId(buyerId, pageable);
        Page<PurchaseResponse> responses = purchases.map(this::mapToResponseWithPost);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByBuyerId(@PathVariable Long buyerId) {
        List<Purchase> purchases = purchaseService.getPurchasesByBuyerId(buyerId);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponseWithPost)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesBySellerId(@PathVariable Long sellerId) {
        List<Purchase> purchases = purchaseService.getPurchasesBySellerId(sellerId);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponseWithPost)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByPostId(@PathVariable Long postId) {
        List<Purchase> purchases = purchaseService.getPurchasesByPostId(postId);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponseWithPost)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseResponse>> getPurchasesByStatus(@PathVariable String status) {
        List<Purchase> purchases = purchaseService.getPurchasesByStatus(status);
        List<PurchaseResponse> responses = purchases.stream()
                .map(this::mapToResponseWithPost)
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

    // Wallet endpoints
    @GetMapping("/wallet/balance")
    public ResponseEntity<Double> getWalletBalance() {
        Long userId = getCurrentUserId();
        Double balance = purchaseService.getWalletBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/wallet/add-funds")
    public ResponseEntity<Void> addFundsToWallet(@RequestParam Double amount) {
        Long userId = getCurrentUserId();
        purchaseService.addFundsToWallet(userId, amount);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new AuthorizationServiceException("User not authenticated");
    }

    private PurchaseResponse mapToResponse(Purchase purchase) {
        // For legacy endpoints, we may not fetch post details; but to avoid null, we could leave post null.
        // Here we construct with post null.
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getBuyerId(),
                purchase.getSellerId(),
                purchase.getPostId(),
                purchase.getAmount(),
                purchase.getStatus(),
                purchase.getNotes(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt(),
                null // post not fetched
        );
    }

    private PurchaseResponse mapToResponseWithPost(Purchase purchase) {
        // Fetch post details via service method to include in response
        PostResponse post = purchaseService.getPostForPurchase(purchase);
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getBuyerId(),
                purchase.getSellerId(),
                purchase.getPostId(),
                purchase.getAmount(),
                purchase.getStatus(),
                purchase.getNotes(),
                purchase.getCreatedAt(),
                purchase.getUpdatedAt(),
                post
        );
    }
}
