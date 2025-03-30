package com.poly.controller.response;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {
    private Long id;
    private String productName;
    private String image;
    private String description;
    private BigDecimal productPrice;
    private Long categoryId;
    // more
}
