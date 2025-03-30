package com.poly.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@ToString
public class ProductCreationRequest implements Serializable {

    @NotBlank(message = "Product name must not be blank")
    private String productName;

    private String image;

    private String description;

    @NotNull(message = "Product price must not be null")
    @Min(value = 0, message = "Product price must be greater than or equal to 0")
    private BigDecimal productPrice;

    @NotNull(message = "Stock quantity must not be null")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    private Integer stockQuantity;

    @NotNull(message = "Category ID must not be null")
    @Min(value = 1, message = "Category ID must be greater than or equal to 1")
    private Long categoryId;
}
