package com.poly.controller.response;

import com.poly.common.OrderStatus;
import com.poly.model.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@Builder
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String paymentMethod; // Thêm trường này
    private Date createdAt;
    private Date updatedAt;
    private UserEntity user;
}