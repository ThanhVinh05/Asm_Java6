package com.poly.controller.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {
    private Long productId;
    @Min(1)
    private Integer quantity;
}
