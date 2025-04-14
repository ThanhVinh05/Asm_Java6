package com.poly.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class TopProductResponse {
    private Long productId;
    private String productName;
    private int quantity; // Total quantity sold
    private BigDecimal revenue; // Total revenue from this product
}
