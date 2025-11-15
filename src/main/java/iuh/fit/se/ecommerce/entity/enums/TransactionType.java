package iuh.fit.se.ecommerce.entity.enums;

public enum TransactionType {
    PAYMENT,        // Thanh toán đơn hàng (chi ra)
    REFUND,         // Hoàn tiền (thu vào)
    DEPOSIT,        // Nạp tiền vào ví (thu vào) - nếu có wallet
    WITHDRAWAL,     // Rút tiền (chi ra) - nếu có wallet
    COMMISSION,     // Hoa hồng (nếu có affiliate)
    DISCOUNT        // Giảm giá được áp dụng
}

