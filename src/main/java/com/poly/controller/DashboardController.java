package com.poly.controller;

import com.poly.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard Controller")
@Slf4j(topic = "DASHBOARD-CONTROLLER")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get Dashboard Statistics", description = "API to retrieve overall dashboard statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Request received for dashboard stats");
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Dashboard statistics retrieved successfully");
            response.put("data", dashboardService.getDashboardStats());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats", e);
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Failed to retrieve dashboard statistics");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/revenue-stats")
    @Operation(summary = "Get Revenue Statistics", description = "API to retrieve revenue data for charts")
    public ResponseEntity<Map<String, Object>> getRevenueStats(
            @RequestParam(defaultValue = "year") String period, // 'year' or 'month'
            @RequestParam int year,
            @RequestParam(required = false) Integer month) { // Optional month for 'month' period
        log.info("Request received for revenue stats - Period: {}, Year: {}, Month: {}", period, year, month);
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Revenue statistics retrieved successfully");
            response.put("data", dashboardService.getRevenueStats(period, year, month));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching revenue stats", e);
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Failed to retrieve revenue statistics");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/top-products")
    @Operation(summary = "Get Top Selling Products", description = "API to retrieve top selling products")
    public ResponseEntity<Map<String, Object>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Request received for top {} products", limit);
        Map<String, Object> response = new LinkedHashMap<>();
        try {
            response.put("status", HttpStatus.OK.value());
            response.put("message", "Top products retrieved successfully");
            response.put("data", dashboardService.getTopProducts(limit));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching top products", e);
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("message", "Failed to retrieve top products");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}