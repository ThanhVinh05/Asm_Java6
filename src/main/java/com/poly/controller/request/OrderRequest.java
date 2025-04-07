package com.poly.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String address;
    private String note;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private List<OrderItemRequest> items;
}
