package iuh.fit.se.ecommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import iuh.fit.se.ecommerce.entity.enums.PaymentMethod;
import iuh.fit.se.ecommerce.entity.enums.TransactionStatus;
import iuh.fit.se.ecommerce.entity.enums.TransactionType;
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
public class TransactionResponse {
    private Long id;
    private String transactionCode;
    private TransactionType type;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String description;
    private Long orderId;
    private Long orderCode;
    private Long paymentId;
    private String externalTransactionId;
    private String userName;
    private String userEmail;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
        
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;
}

