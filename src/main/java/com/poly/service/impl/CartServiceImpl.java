package com.poly.service.impl;

import com.poly.controller.request.CartItemRequest;
import com.poly.controller.response.CartItemResponse;
import com.poly.controller.response.ProductResponse;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.CartEntity;
import com.poly.model.ProductEntity;
import com.poly.repository.CartRepository;
import com.poly.repository.ProductRepository;
import com.poly.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CartItemResponse> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addToCart(Long userId, CartItemRequest request) {
        CartEntity cartItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(new CartEntity());

        if (cartItem.getId() == null) {
            cartItem.setUserId(userId);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        }

        cartRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void updateCartItem(Long userId, CartItemRequest request) {
        CartEntity cartItem = cartRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        cartItem.setQuantity(request.getQuantity());
        cartRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }

    private CartItemResponse convertToResponse(CartEntity cartItem) {
        ProductEntity product = cartItem.getProduct();
        return CartItemResponse.builder()
                .productId(product.getId())
                .quantity(cartItem.getQuantity())
                .product(ProductResponse.builder()
                        .id(product.getId())
                        .productName(product.getProductName())
                        .image(product.getImage())
                        .description(product.getDescription())
                        .productPrice(product.getProductPrice())
                        .categoryId(product.getCategoryId())
                        .build())
                .build();
    }
}
