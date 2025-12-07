package iuh.fit.se.ecommerce.listener;

import iuh.fit.se.ecommerce.entity.*;
import iuh.fit.se.ecommerce.event.OrderConfirmedEvent;
import iuh.fit.se.ecommerce.event.UserRegisteredEvent;
import iuh.fit.se.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsAuditListener {

    private final RevenueAuditRepository revenueAuditRepository;
    private final BuyerAuditRepository buyerAuditRepository;
    private final ProductSaleAuditRepository productSaleAuditRepository;
    private final NewCustomerAuditRepository newCustomerAuditRepository;

    @EventListener
    @Async
    @Transactional
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        Order order = event.getOrder();
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1. Revenue Audit - Ghi nhận doanh số khi order được confirm
            RevenueAudit revenueAudit = RevenueAudit.builder()
                    .orderCode(order.getOrderCode())
                    .userId(order.getUser().getId())
                    .amount(order.getTotalAmount())
                    .status(order.getStatus().name())
                    .recordedAt(now)
                    .build();
            revenueAuditRepository.save(revenueAudit);
            log.debug("Recorded revenue audit for order: {}", order.getOrderCode());

            // 2. Buyer Audit - Ghi nhận người mua (chỉ ghi 1 lần cho mỗi user-order)
            if (!buyerAuditRepository.existsByUserIdAndOrderCode(order.getUser().getId(), order.getOrderCode())) {
                BuyerAudit buyerAudit = BuyerAudit.builder()
                        .userId(order.getUser().getId())
                        .orderCode(order.getOrderCode())
                        .firstPurchaseAt(now)
                        .recordedAt(now)
                        .build();
                buyerAuditRepository.save(buyerAudit);
                log.debug("Recorded buyer audit for user: {}, order: {}", order.getUser().getId(), order.getOrderCode());
            }

            // 3. Product Sale Audit - Ghi nhận từng sản phẩm đã bán
            for (OrderItem item : order.getItems()) {
                ProductSaleAudit saleAudit = ProductSaleAudit.builder()
                        .orderCode(order.getOrderCode())
                        .productId(item.getProduct().getId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .recordedAt(now)
                        .build();
                productSaleAuditRepository.save(saleAudit);
            }
            log.debug("Recorded product sale audit for order: {}", order.getOrderCode());

        } catch (Exception e) {
            log.error("Error recording statistics audit for order: {}", order.getOrderCode(), e);
            // Don't throw exception to avoid affecting order processing
        }
    }

    @EventListener
    @Async
    @Transactional
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        LocalDateTime now = LocalDateTime.now();

        try {
            // New Customer Audit - Ghi nhận tài khoản mới
            if (!newCustomerAuditRepository.existsByUserId(user.getId())) {
                NewCustomerAudit customerAudit = NewCustomerAudit.builder()
                        .userId(user.getId())
                        .registeredAt(user.getCreatedAt() != null ? user.getCreatedAt() : now)
                        .build();
                newCustomerAuditRepository.save(customerAudit);
                log.debug("Recorded new customer audit for user: {}", user.getId());
            }
        } catch (Exception e) {
            log.error("Error recording new customer audit for user: {}", user.getId(), e);
            // Don't throw exception to avoid affecting user registration
        }
    }
}

