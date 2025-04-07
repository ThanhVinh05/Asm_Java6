package com.poly.controller.response;

import com.poly.common.OrderStatus;
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
    private OrderStatus status; // Sử dụng OrderStatus thay vì String
    private BigDecimal totalAmount;
    private Date createdAt;
    private Date updatedAt;
}
