package com.poly.repository;

import com.poly.common.OrderStatus;
import com.poly.model.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
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

    @Query("SELECT o FROM OrderEntity o " +
            "WHERE (:keyword IS NULL OR CAST(o.id AS string) LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(o.note) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.paymentMethod) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:orderId IS NULL OR o.id = :orderId) " +
            "AND (:status IS NULL OR o.status = :status) " +
            "AND (:createdAt IS NULL OR CAST(o.createdAt AS date) = CAST(:createdAt AS date)) " + // Sử dụng CAST thay vì DATE()
            "AND (:totalAmount IS NULL OR o.totalAmount = :totalAmount)")
    Page<OrderEntity> findAllOrders(
            @Param("keyword") String keyword,
            @Param("orderId") Long orderId,
            @Param("status") OrderStatus status,
            @Param("createdAt") Date createdAt,
            @Param("totalAmount") BigDecimal totalAmount,
            Pageable pageable
    );

    // Query để lấy tổng doanh thu theo status
    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.status = :status")
    BigDecimal sumTotalAmountByStatus(@Param("status") OrderStatus status); // Return BigDecimal

    // Query để lấy các đơn hàng COMPLETED trong khoảng thời gian
    // Sắp xếp theo createdAt để dễ xử lý trong service
    @Query("SELECT o FROM OrderEntity o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt ASC")
    List<OrderEntity> findCompletedOrdersBetweenDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}