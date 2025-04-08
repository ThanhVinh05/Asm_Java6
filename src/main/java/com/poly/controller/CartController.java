package com.poly.controller;

import com.poly.controller.request.CartItemRequest;
import com.poly.exception.InvalidDataException;
import com.poly.service.CartService;
import com.poly.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart Controller")
@Slf4j(topic = "CART-CONTROLLER")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public ResponseEntity<Map<String, Object>> getCartItems() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Cart items retrieved successfully");
            response.put("data", cartService.getCartItems(userId));
            return ResponseEntity.ok(response);
        } catch (InvalidDataException e) {
            log.error("Error getting cart items: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error getting cart items: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể lấy thông tin giỏ hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody CartItemRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            cartService.addToCart(userId, request);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Item added to cart successfully");
            return ResponseEntity.ok(response);
        } catch (InvalidDataException e) {
            log.error("Error adding to cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error adding to cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể thêm vào giỏ hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody CartItemRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            cartService.updateCartItem(userId, request);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Cart item updated successfully");
            return ResponseEntity.ok(response);
        } catch (InvalidDataException e) {
            log.error("Error updating cart item: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error updating cart item: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể cập nhật giỏ hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable Long productId) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            cartService.removeFromCart(userId, productId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Item removed from cart successfully");
            return ResponseEntity.ok(response);
        } catch (InvalidDataException e) {
            log.error("Error removing from cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error removing from cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể xóa sản phẩm khỏi giỏ hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart() {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            cartService.clearCart(userId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Cart cleared successfully");
            return ResponseEntity.ok(response);
        } catch (InvalidDataException e) {
            log.error("Error clearing cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error clearing cart: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể xóa giỏ hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}