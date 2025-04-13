package com.poly.service;

import com.poly.common.OrderStatus;
import com.poly.controller.request.OrderRequest;
import com.poly.controller.response.OrderPageResponse;
import com.poly.controller.response.OrderResponse;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public interface OrderService {
    Long createOrder(Long userId, OrderRequest request);

    OrderResponse getOrderById(Long orderId);

    Page<OrderResponse> getUserOrders(Long userId, int page, int size);

    void cancelOrder(Long orderId);

    void updateOrderStatus(Long orderId, String status);
    Map<String, Object> getUserByOrder(Long orderId);

    OrderPageResponse findAll(String keyword, String sort, int page, int size,
                              String orderId, OrderStatus status, Date createdAt, BigDecimal totalAmount);
}