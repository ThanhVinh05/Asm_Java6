package com.poly.service.impl;

import com.poly.common.OrderStatus;
import com.poly.controller.request.OrderRequest;
import com.poly.controller.response.OrderResponse;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.OrderDetailEntity;
import com.poly.model.OrderEntity;
import com.poly.repository.OrderDetailRepository;
import com.poly.repository.OrderRepository;
import com.poly.service.CartService;
import com.poly.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartService cartService;

    @Transactional
    public Long createOrder(Long userId, OrderRequest request) {
        // Create order
        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(request.getTotalAmount());
        order.setNote(request.getNote());
        order.setPaymentMethod(request.getPaymentMethod());

        orderRepository.save(order);

        // Create order details
        List<OrderDetailEntity> orderDetails = request.getItems().stream()
                .map(item -> {
                    OrderDetailEntity detail = new OrderDetailEntity();
                    detail.setOrderId(order.getId());
                    detail.setProductId(item.getProductId());
                    detail.setQuantity(item.getQuantity());
                    detail.setPrice(item.getPrice());
                    return detail;
                })
                .collect(Collectors.toList());

        orderDetailRepository.saveAll(orderDetails);

        return order.getId();
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return convertToResponse(order);
    }

    @Override
    public Page<OrderResponse> getUserOrders(Long userId, int page, int size) {
        return orderRepository.findByUserId(userId, PageRequest.of(page - 1, size))
                .map(this::convertToResponse);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.PENDING) { // So sánh enum trực tiếp
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        } else {
            throw new IllegalStateException("Order cannot be cancelled");
        }
    }

    private OrderResponse convertToResponse(OrderEntity order) {
        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus()) // Không cần chuyển đổi nữa
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
