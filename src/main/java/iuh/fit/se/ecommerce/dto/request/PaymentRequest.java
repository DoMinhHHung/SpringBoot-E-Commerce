package iuh.fit.se.ecommerce.dto.request;

import iuh.fit.se.ecommerce.entity.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    @Valid
    private List<PaymentItemRequest> items;

    private Long shippingAddressId;  // Optional: nếu null sẽ dùng default address

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.PAYOS;

    private String notes;  // Ghi chú đơn hàng
}

