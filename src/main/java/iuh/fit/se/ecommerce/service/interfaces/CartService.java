package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.response.CartResponse;

public interface CartService {
    CartResponse addItem(Long userId, Long productId, int quantity);
    CartResponse updateQuantity(Long userId, Long productId, int quantity);
    void removeItem(Long userId, Long productId);
    CartResponse getCartByUser(Long userId);
    void clearCart(Long userId);
}
