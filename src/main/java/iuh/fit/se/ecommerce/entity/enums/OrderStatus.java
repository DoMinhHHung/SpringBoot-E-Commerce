package iuh.fit.se.ecommerce.entity.enums;

public enum OrderStatus {
    PENDING,        // Đang chờ thanh toán
    CONFIRMED,      // Đã xác nhận (đã thanh toán)
    PROCESSING,     // Đang xử lý
    SHIPPED,        // Đã giao hàng
    DELIVERED,      // Đã nhận hàng
    CANCELLED,      // Đã hủy
    REFUNDED        // Đã hoàn tiền
}

