package com.poly.controller;

import com.poly.controller.request.CartItemRequest;
import com.poly.service.CartService;
import com.poly.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItems() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Cart items retrieved successfully");
        response.put("data", cartService.getCartItems(userId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody CartItemRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.addToCart(userId, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Item added to cart successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody CartItemRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.updateCartItem(userId, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Cart item updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long productId) {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.removeFromCart(userId, productId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Item removed from cart successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        cartService.clearCart(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Cart cleared successfully");
        return ResponseEntity.ok(response);
    }
}
