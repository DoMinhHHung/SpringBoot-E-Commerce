package iuh.fit.se.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryResponse {
    private Long totalTransactions;
    private Long pendingTransactions;
    private BigDecimal totalRevenue;      // Tổng thu (số dương)
    private BigDecimal totalExpense;      // Tổng chi (số âm, nhưng lưu dương)
    private BigDecimal netAmount;          // Số dư cuối cùng (revenue - expense)
}

