package com.example.avito.controller;

import com.example.avito.entity.Purchase;
import com.example.avito.mapper.PurchaseMapper;
import com.example.avito.response.PurchaseResponse;
import com.example.avito.service.PurchaseService;
import com.example.avito.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tag(name = "Покупки", description = "API для управления покупками")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseMapper purchaseMapper;
    private final UserService userService;

    @Operation(
        summary = "Купить объявление",
        description = "Позволяет пользователю купить объявление, если у него достаточно средств на балансе",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Покупка успешно совершена",
                content = @Content(schema = @Schema(implementation = PurchaseResponse.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Ошибка валидации (недостаточно средств, объявление уже куплено и т.д.)"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Объявление или пользователь не найдены"
            )
        }
    )
    @PostMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PurchaseResponse> purchasePost(
            @Parameter(description = "ID объявления", in = ParameterIn.PATH, required = true)
            @PathVariable Long postId) {

        // Получаем текущего пользователя
        var currentUserResponse = userService.getCurrentUser();
        Long buyerId = currentUserResponse.getId();

        Purchase purchase = purchaseService.purchasePost(postId, buyerId);
        PurchaseResponse response = purchaseMapper.toResponse(purchase);

        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Получить список покупок пользователя",
        description = "Возвращает список всех покупок текущего пользователя",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Список покупок успешно получен",
                content = @Content(schema = @Schema(implementation = PurchaseResponse.class, type = "array"))
            )
        }
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PurchaseResponse>> getUserPurchases(Pageable pageable) {
        var currentUserResponse = userService.getCurrentUser();
        Long userId = currentUserResponse.getId();

        Page<Purchase> purchases = purchaseService.getPurchasesByUser(userId, pageable);
        Page<PurchaseResponse> responses = purchases.map(purchaseMapper::toResponse);

        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "Пополнить баланс",
        description = "Пополняет баланс пользователя на указанную сумму",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Баланс успешно пополнен"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Некорректная сумма"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Пользователь не найден"
            )
        }
    )
    @PostMapping("/wallet/add-funds")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addFundsToWallet(
            @Parameter(description = "Сумма для пополнения", required = true)
            @RequestParam BigDecimal amount) {

        var currentUserResponse = userService.getCurrentUser();
        Long userId = currentUserResponse.getId();

        purchaseService.addFundsToWallet(userId, amount);

        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Получить баланс пользователя",
        description = "Возвращает текущий баланс пользователя",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Баланс успешно получен",
                content = @Content(schema = @Schema(implementation = BigDecimal.class))
            )
        }
    )
    @GetMapping("/wallet/balance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getWalletBalance() {
        var currentUserResponse = userService.getCurrentUser();
        Long userId = currentUserResponse.getId();

        BigDecimal balance = userService.getUserWalletBalance(userId);

        return ResponseEntity.ok(balance);
    }
}