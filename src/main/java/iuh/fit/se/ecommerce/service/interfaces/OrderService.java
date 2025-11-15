package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.OrderUpdateRequest;
import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.dto.response.OrderDetailResponse;
import iuh.fit.se.ecommerce.dto.response.OrderResponse;
import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    // Existing methods
    Order createOrder(PaymentRequest request, User user);
    Order confirmOrder(Long orderCode);
    Order getOrderByCode(Long orderCode);
    
    // New methods for user
    Page<OrderResponse> getUserOrders(String userEmail, OrderStatus status, Pageable pageable);
    OrderDetailResponse getOrderDetail(Long orderCode, String userEmail);
    
    // New methods for admin
    Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable);
    OrderDetailResponse getOrderDetailForAdmin(Long orderCode);
    OrderResponse updateOrderStatus(Long orderCode, OrderStatus newStatus, String notes);
}

