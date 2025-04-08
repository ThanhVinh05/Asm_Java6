package com.poly.controller.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private String note;
    @NotEmpty
    private String paymentMethod;
    @DecimalMin("0.01")
    private BigDecimal totalAmount;
    @NotEmpty
    private List<OrderItemRequest> items;
}
