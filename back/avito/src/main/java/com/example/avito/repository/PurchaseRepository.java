package com.example.avito.repository;

import com.example.avito.entity.Purchase;
import com.example.avito.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Page<Purchase> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    List<Purchase> findByBuyerId(Long buyerId);
    boolean existsByBuyerIdAndPostId(Long buyerId, Long postId);
}