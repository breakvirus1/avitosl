package com.avitosl.purchaseservice.service;

import com.avitosl.purchaseservice.entity.Purchase;
import com.avitosl.purchaseservice.exception.ConflictException;
import com.avitosl.purchaseservice.exception.NotFoundException;
import com.avitosl.purchaseservice.feign.PostServiceClient;
import com.avitosl.purchaseservice.feign.UserServiceClient;
import com.avitosl.purchaseservice.mapper.PurchaseMapper;
import com.avitosl.purchaseservice.repository.PurchaseRepository;
import com.avitosl.purchaseservice.response.PostResponse;
import com.avitosl.purchaseservice.response.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseMapper purchaseMapper;
    private final UserServiceClient userServiceClient;
    private final PostServiceClient postServiceClient;

    public Purchase createPurchase(Purchase purchase) {
        // Проверяем существование покупателя (buyer)
        try {
            userServiceClient.getUserById(purchase.getBuyerId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Buyer not found with id: " + purchase.getBuyerId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch buyer: " + e.getMessage());
        }
        
        // Проверяем существование продавца (seller)
        try {
            userServiceClient.getUserById(purchase.getSellerId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Seller not found with id: " + purchase.getSellerId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch seller: " + e.getMessage());
        }
        
        // Проверяем существование поста
        try {
            postServiceClient.getPostById(purchase.getPostId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Post not found with id: " + purchase.getPostId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch post: " + e.getMessage());
        }
        
        return purchaseRepository.save(purchase);
    }

    public Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase not found with id: " + id));
    }

    public List<Purchase> getPurchasesByBuyerId(Long buyerId) {
        return purchaseRepository.findByBuyerId(buyerId);
    }

    public List<Purchase> getPurchasesBySellerId(Long sellerId) {
        return purchaseRepository.findBySellerId(sellerId);
    }

    public List<Purchase> getPurchasesByPostId(Long postId) {
        return purchaseRepository.findByPostId(postId);
    }

    public List<Purchase> getPurchasesByStatus(String status) {
        return purchaseRepository.findByStatus(status);
    }

    public Purchase updatePurchase(Long id, Purchase purchaseDetails) {
        Purchase purchase = getPurchaseById(id);

        purchaseMapper.updateEntityFromRequest(null, purchase);
        purchase.setBuyerId(purchaseDetails.getBuyerId());
        purchase.setSellerId(purchaseDetails.getSellerId());
        purchase.setPostId(purchaseDetails.getPostId());
        purchase.setAmount(purchaseDetails.getAmount());
        purchase.setStatus(purchaseDetails.getStatus());
        purchase.setNotes(purchaseDetails.getNotes());

        return purchaseRepository.save(purchase);
    }

    public void deletePurchase(Long id) {
        Purchase purchase = getPurchaseById(id);
        purchaseRepository.delete(purchase);
    }

    public Purchase updatePurchaseStatus(Long id, String status) {
        Purchase purchase = getPurchaseById(id);
        purchase.setStatus(status);
        return purchaseRepository.save(purchase);
    }
}
