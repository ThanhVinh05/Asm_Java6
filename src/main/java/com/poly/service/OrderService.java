package com.poly.service;

import com.poly.controller.request.OrderRequest;
import com.poly.controller.response.OrderResponse;
import org.springframework.data.domain.Page;

public interface OrderService {
    Long createOrder(Long userId, OrderRequest request);

    OrderResponse getOrderById(Long orderId);

    Page<OrderResponse> getUserOrders(Long userId, int page, int size);

    void cancelOrder(Long orderId);
}
