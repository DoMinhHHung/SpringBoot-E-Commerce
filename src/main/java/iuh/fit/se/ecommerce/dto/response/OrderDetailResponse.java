package iuh.fit.se.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private Long orderCode;
    private OrderStatus status;
    private String statusLabel; // Label tiếng Việt
    private Integer progressStep; // 0-3 cho progress bar
    
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal totalAmount;
    private String notes;
    
    private AddressResponse shippingAddress;
    private List<OrderItemResponse> items;
    
    // Payment info
    private PaymentInfo paymentInfo;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime confirmedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime shippedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveredAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String paymentMethod;
        private String paymentStatus;
        private String transactionId;
    }
}

