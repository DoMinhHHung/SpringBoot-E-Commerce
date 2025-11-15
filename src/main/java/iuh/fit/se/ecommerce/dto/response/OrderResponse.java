package iuh.fit.se.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private Long orderCode;
    private OrderStatus status;
    private String statusLabel; // Label tiếng Việt
    private BigDecimal totalAmount;
    private Integer itemCount; // Số lượng sản phẩm
    private String userName; // Tên khách hàng
    private String userEmail; // Email khách hàng
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

