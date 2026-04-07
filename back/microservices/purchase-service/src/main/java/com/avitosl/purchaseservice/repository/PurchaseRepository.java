package com.avitosl.purchaseservice.repository;

import com.avitosl.purchaseservice.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByBuyerId(Long buyerId);
    List<Purchase> findBySellerId(Long sellerId);
    List<Purchase> findByPostId(Long postId);
    List<Purchase> findByStatus(String status);
}
