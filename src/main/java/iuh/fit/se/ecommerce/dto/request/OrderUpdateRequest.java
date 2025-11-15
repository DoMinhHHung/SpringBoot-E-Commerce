package iuh.fit.se.ecommerce.dto.request;

import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateRequest {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;
    
    private String notes; // Ghi chú thay đổi
}

