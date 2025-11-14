package iuh.fit.se.ecommerce.entity.enums;

public enum PaymentStatus {
    PENDING,        // Đang chờ thanh toán
    PAID,           // Đã thanh toán
    CANCELLED,      // Đã hủy
    FAILED,         // Thanh toán thất bại
    REFUNDED        // Đã hoàn tiền
}

