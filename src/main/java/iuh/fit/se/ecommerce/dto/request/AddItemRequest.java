package iuh.fit.se.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddItemRequest {
    private Long userId;
    private Long productId;
    private int quantity;
}
