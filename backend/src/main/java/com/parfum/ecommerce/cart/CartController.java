package com.parfum.ecommerce.cart;

import com.parfum.ecommerce.cart.dto.AddItemRequest;
import com.parfum.ecommerce.cart.dto.CartResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart(Authentication auth) {
        return cartService.getCart(auth.getName());
    }

    @PostMapping("/items")
    public CartResponse addItem(Authentication auth, @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(auth.getName(), request);
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(Authentication auth, @PathVariable UUID itemId, @RequestBody Map<String, Integer> body) {
        return cartService.updateItemQuantity(auth.getName(), itemId, body.get("quantity"));
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(Authentication auth, @PathVariable UUID itemId) {
        return cartService.removeItem(auth.getName(), itemId);
    }
}