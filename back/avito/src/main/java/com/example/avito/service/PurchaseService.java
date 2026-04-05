package com.example.avito.service;

import com.example.avito.entity.Post;
import com.example.avito.entity.Purchase;
import com.example.avito.entity.User;
import com.example.avito.exception.NotFoundException;
import com.example.avito.exception.ValidationException;
import com.example.avito.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserService userService;
    private final PostService postService;

    @Transactional
    public Purchase purchasePost(Long postId, Long buyerId) {
        User buyer = userService.findById(buyerId)
                .orElseThrow(() -> new NotFoundException("Покупатель не найден"));

        Post post = postService.findById(postId)
                .orElseThrow(() -> new NotFoundException("Объявление не найдено"));

        // Проверяем, что объявление активно
        if (!post.isActive()) {
            throw new ValidationException("Объявление не активно");
        }

        // Проверяем, что покупатель не является автором объявления
        if (post.getAuthor().getId().equals(buyerId)) {
            throw new ValidationException("Нельзя купить свое собственное объявление");
        }

        // Проверяем, что у покупателя достаточно средств
        if (buyer.getWalletBalance().compareTo(post.getPrice()) < 0) {
            throw new ValidationException("Недостаточно средств на балансе");
        }

        // Проверяем, что объявление еще не куплено
        if (purchaseRepository.existsByBuyerIdAndPostId(buyerId, postId)) {
            throw new ValidationException("Это объявление уже куплено");
        }

        // Создаем покупку
        Purchase purchase = Purchase.builder()
                .buyer(buyer)
                .post(post)
                .purchasePrice(post.getPrice())
                .build();

        purchase = purchaseRepository.save(purchase);

        // Списываем средства с баланса покупателя
        buyer.setWalletBalance(buyer.getWalletBalance().subtract(post.getPrice()));
        userService.saveUser(buyer);

        // Добавляем средства на баланс продавца
        User seller = post.getAuthor();
        seller.setWalletBalance(seller.getWalletBalance().add(post.getPrice()));
        userService.saveUser(seller);

        // Деактивируем объявление (оно продано)
        post.setActive(false);
        postService.savePost(post);

        return purchase;
    }

    public Page<Purchase> getPurchasesByUser(Long userId, Pageable pageable) {
        return purchaseRepository.findByBuyerIdOrderByCreatedAtDesc(userId, pageable);
    }

    public boolean hasUserPurchasedPost(Long userId, Long postId) {
        return purchaseRepository.existsByBuyerIdAndPostId(userId, postId);
    }

    @Transactional
    public void addFundsToWallet(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Сумма должна быть положительной");
        }

        User user = userService.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setWalletBalance(user.getWalletBalance().add(amount));
        userService.saveUser(user);
    }
}