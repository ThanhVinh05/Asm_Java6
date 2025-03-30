package com.poly.controller.response;

import com.poly.common.Gender;
import jakarta.persistence.Column;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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
    // more
}
