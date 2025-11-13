package iuh.fit.se.ecommerce.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoveItemRequest {
    private Long userId;
    private Long productId;
}
