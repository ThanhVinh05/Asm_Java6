package com.poly.common;

public enum OrderStatus {
    PENDING,        // Đơn hàng mới tạo
    CONFIRMED,      // Đã xác nhận
    PROCESSING,     // Đang xử lý
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao hàng
    COMPLETED,      // Hoàn thành
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}