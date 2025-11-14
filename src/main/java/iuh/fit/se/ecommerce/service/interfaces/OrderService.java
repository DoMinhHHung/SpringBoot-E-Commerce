package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.User;

public interface OrderService {
    Order createOrder(PaymentRequest request, User user);
    Order confirmOrder(Long orderCode);
    Order getOrderByCode(Long orderCode);
}

