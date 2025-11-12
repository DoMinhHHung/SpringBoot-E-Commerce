package iuh.fit.se.ecommerce.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long userId;
    private List<CartItemResponse> items;
    private BigDecimal totalPrice;
}

