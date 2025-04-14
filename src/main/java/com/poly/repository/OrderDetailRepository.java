package com.poly.repository;

import com.poly.model.OrderDetailEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetailEntity, Long> {
    // Tìm chi tiết đơn hàng theo orderId
    List<OrderDetailEntity> findByOrderId(Long orderId);

    // Tìm chi tiết đơn hàng theo productId
    List<OrderDetailEntity> findByProductId(Long productId);

    // Tìm chi tiết đơn hàng theo orderId và productId
    OrderDetailEntity findByOrderIdAndProductId(Long orderId, Long productId);

    // Đếm số lượng sản phẩm trong một đơn hàng
    long countByOrderId(Long orderId);

    // Tính tổng tiền của một đơn hàng
    @Query("SELECT SUM(od.price * od.quantity) FROM OrderDetailEntity od WHERE od.orderId = :orderId")
    Double calculateOrderTotal(@Param("orderId") Long orderId);

    // Lấy top sản phẩm bán chạy
    @Query("SELECT od.productId, SUM(od.quantity) as total FROM OrderDetailEntity od " +
            "GROUP BY od.productId ORDER BY total DESC")
    List<Object[]> findTopSellingProducts();

    // Xóa tất cả chi tiết đơn hàng theo orderId
    void deleteByOrderId(Long orderId);

    // Query để lấy thông tin chi tiết đơn hàng kèm thông tin sản phẩm
    @Query("SELECT od FROM OrderDetailEntity od LEFT JOIN FETCH od.product WHERE od.orderId = :orderId")
    List<OrderDetailEntity> findOrderDetailsWithProduct(@Param("orderId") Long orderId);

    @Query("SELECT p.id, p.productName, SUM(od.quantity) as totalQuantity, SUM(od.price * od.quantity) as totalRevenue " +
            "FROM OrderDetailEntity od JOIN od.product p JOIN od.order o " +
            "WHERE o.status = 'COMPLETED' " +
            "GROUP BY p.id, p.productName ORDER BY totalQuantity DESC, totalRevenue DESC")
    List<Object[]> findTopSellingProductsWithDetails(Pageable pageable);
}
