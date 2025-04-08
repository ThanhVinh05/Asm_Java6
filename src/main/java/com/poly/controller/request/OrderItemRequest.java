package com.poly.controller.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
    @DecimalMin("0.01")
    private BigDecimal price;
}
