package com.poly.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemResponse {
    private Long productId;
    private Integer quantity;
    private ProductResponse product;
}
