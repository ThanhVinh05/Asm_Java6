package com.poly.repository;

import com.poly.model.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    // Tìm đơn hàng theo userId
    List<OrderEntity> findByUserId(Long userId);

    // Tìm đơn hàng theo userId với phân trang
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    // Tìm đơn hàng theo userId và id
    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

    // Đếm số đơn hàng của một user
    long countByUserId(Long userId);

    // Tìm đơn hàng theo status
    List<OrderEntity> findByStatus(String status);

    // Tìm đơn hàng theo userId và status
    List<OrderEntity> findByUserIdAndStatus(Long userId, String status);

    // Query tùy chỉnh để lấy tổng doanh thu theo userId
    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.userId = :userId AND o.status = 'COMPLETED'")
    Double getTotalAmountByUserId(@Param("userId") Long userId);

    // Query để lấy các đơn hàng trong khoảng thời gian
    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<OrderEntity> findOrdersBetweenDates(@Param("startDate") String startDate, @Param("endDate") String endDate);
}