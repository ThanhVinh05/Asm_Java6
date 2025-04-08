package com.poly.controller;

import com.poly.controller.request.OrderRequest;
import com.poly.model.OrderDetailEntity;
import com.poly.repository.OrderDetailRepository;
import com.poly.service.OrderService;
import com.poly.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@Tag(name = "Order Controller")
@Slf4j(topic = "ORDER-CONTROLLER")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailRepository orderDetailRepository;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest request) {
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            Long orderId = orderService.createOrder(userId, request);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Order created successfully");
            response.put("data", orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể tạo đơn hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
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
    @GetMapping("/details/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetailsWithProducts(@PathVariable Long orderId) {
        try {
            List<OrderDetailEntity> details = orderDetailRepository.findOrderDetailsWithProduct(orderId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Order details retrieved successfully");
            response.put("data", details);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Error retrieving order details");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
