package com.poly.controller;

import com.poly.common.OrderStatus;
import com.poly.controller.request.OrderRequest;
import com.poly.controller.response.OrderPageResponse;
import com.poly.model.OrderDetailEntity;
import com.poly.repository.OrderDetailRepository;
import com.poly.service.OrderService;
import com.poly.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrderUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "User orders retrieved successfully");
            response.put("data", orderService.getUserOrders(userId, page, size));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving orders for user {}: {}", userId, e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể lấy danh sách đơn hàng của người dùng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long orderId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            orderService.updateOrderStatus(orderId, status);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "Order status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating order status: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể cập nhật trạng thái đơn hàng");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/userDetails/{orderId}")
    public ResponseEntity<Map<String, Object>> getUserByOrder(@PathVariable Long orderId) {
        try {
            Map<String, Object> user = orderService.getUserByOrder(orderId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", 200);
            response.put("message", "User details retrieved successfully");
            response.put("data", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving user by order: {}", e.getMessage());
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "Không thể lấy thông tin người dùng");
            return ResponseEntity.status(500).body(errorResponse);
        }
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

    @GetMapping("/list")  // URL riêng
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date createdAt,
            @RequestParam(required = false) BigDecimal totalAmount) {
        OrderPageResponse orderPageResponse = orderService.findAll(keyword, sort, page, size, orderId, status, createdAt, totalAmount);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Order list retrieved successfully");
        response.put("data", orderPageResponse);
        return ResponseEntity.ok(response);
    }
}