package com.avitosl.purchaseservice.service;

import com.avitosl.purchaseservice.entity.Purchase;
import com.avitosl.purchaseservice.exception.ConflictException;
import com.avitosl.purchaseservice.exception.NotFoundException;
import com.avitosl.purchaseservice.feign.PostServiceClient;
import com.avitosl.purchaseservice.feign.UserServiceClient;
import com.avitosl.purchaseservice.mapper.PurchaseMapper;
import com.avitosl.purchaseservice.repository.PurchaseRepository;
import com.avitosl.purchaseservice.response.PostResponse;
import com.avitosl.purchaseservice.response.PurchaseResponse;
import com.avitosl.purchaseservice.response.UserResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        // Validation of existence
        try {
            userServiceClient.getUserById(purchase.getBuyerId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Buyer not found with id: " + purchase.getBuyerId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch buyer: " + e.getMessage());
        }

        try {
            userServiceClient.getUserById(purchase.getSellerId());
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Seller not found with id: " + purchase.getSellerId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch seller: " + e.getMessage());
        }

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

    public Page<Purchase> getPurchasesByBuyerId(Long buyerId, Pageable pageable) {
        return purchaseRepository.findByBuyerId(buyerId, pageable);
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

    /**
     * Main purchase flow: buyer purchases a post.
     */
    @Transactional
    public PurchaseResponse purchasePost(Long postId, Long buyerId) {
        // Fetch post
        PostResponse post;
        try {
            post = postServiceClient.getPostById(postId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Post not found with id: " + postId);
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch post: " + e.getMessage());
        }

        if (post == null || !Boolean.TRUE.equals(post.getIsActive())) {
            throw new NotFoundException("Post is not active or does not exist");
        }

        // Get seller's keycloakId from post
        String sellerKeycloakId = post.getKeycloakId();
        if (sellerKeycloakId == null) {
            throw new IllegalStateException("Post has no seller information");
        }

        // Get seller's internal user ID
        UserResponse seller;
        try {
            seller = userServiceClient.getUserByKeycloakId(sellerKeycloakId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Seller not found");
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch seller: " + e.getMessage());
        }
        Long sellerId = seller.getId();

        // Prevent self-purchase
        if (buyerId.equals(sellerId)) {
            throw new ConflictException("Cannot purchase your own post");
        }

        // Get buyer's wallet balance
        UserResponse buyer;
        try {
            buyer = userServiceClient.getUserById(buyerId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("Buyer not found with id: " + buyerId);
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch buyer: " + e.getMessage());
        }
        Double buyerBalance = buyer.getWalletBalance();
        if (buyerBalance == null) buyerBalance = 0.0;

        Double price = post.getPrice();
        if (price == null) price = 0.0;

        if (buyerBalance < price) {
            throw new ConflictException("Insufficient funds in wallet");
        }

        // Deduct from buyer
        try {
            userServiceClient.subtractFromWallet(buyerId, price);
        } catch (FeignException e) {
            throw new RuntimeException("Failed to deduct funds from buyer: " + e.getMessage());
        }

        // Credit seller
        try {
            userServiceClient.addFundsToWallet(sellerId, price);
        } catch (FeignException e) {
            // If crediting fails, we have already deducted buyer; attempt to refund buyer? 
            // For now, throw exception; data may be inconsistent, but that's a limitation.
            throw new RuntimeException("Failed to add funds to seller: " + e.getMessage());
        }

        // Create purchase record
        Purchase purchase = new Purchase();
        purchase.setBuyerId(buyerId);
        purchase.setSellerId(sellerId);
        purchase.setPostId(postId);
        purchase.setAmount(price);
        purchase.setStatus("COMPLETED");
        purchase.setNotes("Purchase completed");
        Purchase saved = purchaseRepository.save(purchase);

        // Deactivate the post after purchase
        try {
            postServiceClient.deactivatePost(postId);
        } catch (FeignException e) {
            // Log but don't fail the purchase if deactivation fails
            // In a real system, you might have a retry mechanism or event
        }

        // Build detailed response
        PurchaseResponse response = new PurchaseResponse();
        response.setId(saved.getId());
        response.setBuyerId(saved.getBuyerId());
        response.setSellerId(saved.getSellerId());
        response.setPostId(saved.getPostId());
        response.setPurchasePrice(saved.getAmount());
        response.setStatus(saved.getStatus());
        response.setNotes(saved.getNotes());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedAt(saved.getUpdatedAt());
        response.setPost(post);

        return response;
    }

    /**
     * Adds funds to user's wallet.
     */
    public UserResponse addFundsToWallet(Long userId, Double amount) {
        try {
            return userServiceClient.addFundsToWallet(userId, amount);
        } catch (FeignException e) {
            throw new RuntimeException("Failed to add funds: " + e.getMessage());
        }
    }

    /**
     * Returns wallet balance for a user.
     */
    public Double getWalletBalance(Long userId) {
        try {
            UserResponse user = userServiceClient.getUserById(userId);
            return user.getWalletBalance();
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch wallet balance: " + e.getMessage());
        }
    }

    public PostResponse getPostForPurchase(Purchase purchase) {
        try {
            return postServiceClient.getPostById(purchase.getPostId());
        } catch (FeignException e) {
            throw new RuntimeException("Failed to fetch post: " + e.getMessage());
        }
    }
}
