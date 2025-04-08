package com.poly.common;

public enum OrderStatus {
    PENDING,        // Đơn hàng mới tạo
    CONFIRMED,      // Đã xác nhận
    SHIPPING,       // Đang giao hàng
    DELIVERED,      // Đã giao hàng
    COMPLETED,      // Hoàn thành
    CANCELLED,      // Đã hủy
}