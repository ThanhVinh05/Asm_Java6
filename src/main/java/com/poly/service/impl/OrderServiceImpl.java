package com.poly.service.impl;

import com.poly.common.OrderStatus;
import com.poly.controller.request.OrderRequest;
import com.poly.controller.response.OrderPageResponse;
import com.poly.controller.response.OrderResponse;
import com.poly.controller.response.UserResponse;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.OrderDetailEntity;
import com.poly.model.OrderEntity;
import com.poly.repository.OrderDetailRepository;
import com.poly.repository.OrderRepository;
import com.poly.service.OrderService;
import com.poly.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "ORDER-SERVICE")
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserService userService;

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
                .userId(order.getUserId()) // Thêm userId
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
    }

    @Override
    public Map<String, Object> getUserByOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        UserResponse userResponse = userService.findById(order.getUserId());

        Map<String, Object> userDetails = new LinkedHashMap<>();
        userDetails.put("id", userResponse.getId());
        userDetails.put("username", userResponse.getUsername());
        userDetails.put("email", userResponse.getEmail());
        userDetails.put("phone", userResponse.getPhone());
        userDetails.put("addresses", userResponse.getAddresses());

        return userDetails;
    }

    @Override
    public OrderPageResponse findAll(String keyword, String sort, int page, int size, String orderId, OrderStatus status, Date createdAt, BigDecimal totalAmount) {
        Sort.Order order;

        // Xử lý sắp xếp theo các tùy chọn từ UI
        if (StringUtils.hasLength(sort)) {
            // Newest/Oldest
            if (sort.equals("newest")) {
                order = new Sort.Order(Sort.Direction.DESC, "createdAt");
            } else if (sort.equals("oldest")) {
                order = new Sort.Order(Sort.Direction.ASC, "createdAt");
            }
            // Highest/Lowest amount
            else if (sort.equals("highestAmount")) {
                order = new Sort.Order(Sort.Direction.DESC, "totalAmount");
            } else if (sort.equals("lowestAmount")) {
                order = new Sort.Order(Sort.Direction.ASC, "totalAmount");
            } else {
                // Xử lý sort pattern "column:direction"
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sort);
                if (matcher.find()) {
                    String columnName = matcher.group(1);
                    if (matcher.group(3).equalsIgnoreCase("asc")) {
                        order = new Sort.Order(Sort.Direction.ASC, columnName);
                    } else {
                        order = new Sort.Order(Sort.Direction.DESC, columnName);
                    }
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, "createdAt");
                }
            }
        } else {
            order = new Sort.Order(Sort.Direction.DESC, "createdAt");
        }

        // Xử lý phân trang
        int pageNo = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        // Gọi repository để lấy dữ liệu
        Page<OrderEntity> orderEntities = orderRepository.findAllOrders(
                keyword,
                StringUtils.hasLength(orderId) ? Long.parseLong(orderId) : null,
                status,
                createdAt,
                totalAmount,
                pageable
        );

        return getOrderPageResponse(page, size, orderEntities);
    }

    private OrderPageResponse getOrderPageResponse(int page, int size, Page<OrderEntity> orderEntities) {
        List<OrderResponse> orderList = orderEntities.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        OrderPageResponse response = new OrderPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(orderEntities.getTotalElements());
        response.setTotalPages(orderEntities.getTotalPages());
        response.setOrders(orderList); // Đảm bảo dòng này đã được thêm

        return response;
    }
}