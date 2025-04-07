package com.poly.service;

import com.poly.controller.request.CartItemRequest;
import com.poly.controller.response.CartItemResponse;

import java.util.List;

public interface CartService {
    List<CartItemResponse> getCartItems(Long userId);

    void addToCart(Long userId, CartItemRequest request);

    void updateCartItem(Long userId, CartItemRequest request);

    void removeFromCart(Long userId, Long productId);

    void clearCart(Long userId);
}
