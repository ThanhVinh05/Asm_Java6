package com.poly.service;

import com.poly.controller.response.DashboardStatsResponse;
import com.poly.controller.response.RevenueStatsResponse;
import com.poly.controller.response.TopProductResponse;

import java.util.List;

public interface DashboardService {
    DashboardStatsResponse getDashboardStats();
    RevenueStatsResponse getRevenueStats(String period, int year, Integer month);
    List<TopProductResponse> getTopProducts(int limit);
}
