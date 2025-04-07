package com.poly.controller;

import com.poly.controller.request.OrderRequest;
import com.poly.service.OrderService;
import com.poly.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Long orderId = orderService.createOrder(userId, request);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Order created successfully");
        response.put("data", orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable Long orderId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Order details retrieved successfully");
        response.put("data", orderService.getOrderById(orderId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/list")
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "User orders retrieved successfully");
        response.put("data", orderService.getUserOrders(userId, page, size));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", 200);
        response.put("message", "Order cancelled successfully");
        return ResponseEntity.ok(response);
    }
}
