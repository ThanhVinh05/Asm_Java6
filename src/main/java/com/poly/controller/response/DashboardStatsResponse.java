package com.poly.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class DashboardStatsResponse {
    private long totalOrders;
    private long totalUsers;
    private long totalProducts;
    private BigDecimal totalRevenue;
}
