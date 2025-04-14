// src/main/java/com/poly/service/impl/DashboardServiceImpl.java
package com.poly.service.impl;

import com.poly.common.OrderStatus;
import com.poly.common.Status;
import com.poly.controller.response.DashboardStatsResponse;
import com.poly.controller.response.RevenueStatsResponse;
import com.poly.controller.response.TopProductResponse;
import com.poly.model.OrderEntity;
import com.poly.repository.OrderDetailRepository;
import com.poly.repository.OrderRepository;
import com.poly.repository.ProductRepository;
import com.poly.repository.UserRepository;
import com.poly.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j(topic = "DASHBOARD-SERVICE")
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        log.info("Fetching dashboard statistics");
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.countByStatus(Status.ACTIVE); // Count only active users
        long totalProducts = productRepository.countByStatus(Status.ACTIVE); // Count only active products
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatus(OrderStatus.COMPLETED);

        return DashboardStatsResponse.builder()
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .build();
    }

    @Override
    public RevenueStatsResponse getRevenueStats(String period, int year, Integer month) {
        log.info("Fetching revenue statistics for Period: {}, Year: {}, Month: {}", period, year, month);
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now(); // End date is always now

        DateTimeFormatter labelFormatter;
        Map<String, BigDecimal> revenueByTimeUnit = new TreeMap<>(); // Use TreeMap to keep order

        if ("month".equalsIgnoreCase(period)) {
            if (month == null || month < 1 || month > 12) {
                month = LocalDate.now().getMonthValue(); // Default to current month if invalid
            }
            YearMonth yearMonth = YearMonth.of(year, month);
            startDate = yearMonth.atDay(1).atStartOfDay();
            endDate = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
            labelFormatter = DateTimeFormatter.ofPattern("dd/MM");

            // Initialize map with all days of the month
            LocalDate currentDay = startDate.toLocalDate();
            while (!currentDay.isAfter(endDate.toLocalDate())) {
                revenueByTimeUnit.put(currentDay.format(labelFormatter), BigDecimal.ZERO);
                currentDay = currentDay.plusDays(1);
            }

        } else { // Default to year
            startDate = YearMonth.of(year, 1).atDay(1).atStartOfDay();
            endDate = YearMonth.of(year, 12).atEndOfMonth().atTime(LocalTime.MAX);
            labelFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

            // Initialize map with all months of the year
            YearMonth currentMonth = YearMonth.from(startDate);
            while (!currentMonth.isAfter(YearMonth.from(endDate))) {
                revenueByTimeUnit.put(currentMonth.format(labelFormatter), BigDecimal.ZERO);
                currentMonth = currentMonth.plusMonths(1);
            }
        }

        log.info("Fetching completed orders between {} and {}", startDate, endDate);
        List<OrderEntity> completedOrders = orderRepository.findCompletedOrdersBetweenDates(
                Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDate.atZone(ZoneId.systemDefault()).toInstant())
        );
        log.info("Found {} completed orders in the date range", completedOrders.size());

        // Aggregate revenue
        for (OrderEntity order : completedOrders) {
            LocalDateTime orderDate = order.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String timeUnitLabel = orderDate.format(labelFormatter);
            revenueByTimeUnit.compute(timeUnitLabel, (key, currentSum) ->
                    (currentSum == null ? BigDecimal.ZERO : currentSum).add(order.getTotalAmount())
            );
        }

        List<String> labels = new ArrayList<>(revenueByTimeUnit.keySet());
        List<BigDecimal> data = new ArrayList<>(revenueByTimeUnit.values());

        RevenueStatsResponse.Dataset dataset = RevenueStatsResponse.Dataset.builder()
                .label("Doanh thu")
                .data(data)
                .build();

        return RevenueStatsResponse.builder()
                .labels(labels)
                .datasets(Collections.singletonList(dataset))
                .build();
    }

    @Override
    public List<TopProductResponse> getTopProducts(int limit) {
        log.info("Fetching top {} selling products", limit);
        Pageable pageable = PageRequest.of(0, limit); // Get top 'limit' products
        List<Object[]> results = orderDetailRepository.findTopSellingProductsWithDetails(pageable);

        return results.stream()
                .map(result -> TopProductResponse.builder()
                        .productId((Long) result[0])
                        .productName((String) result[1])
                        .quantity(((Number) result[2]).intValue()) // Sum of quantity is likely Long or BigInteger
                        .revenue((BigDecimal) result[3]) // Sum of revenue
                        .build())
                .collect(Collectors.toList());
    }
}