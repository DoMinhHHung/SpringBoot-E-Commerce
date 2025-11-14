package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.entity.*;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.*;
import iuh.fit.se.ecommerce.service.interfaces.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Order createOrder(PaymentRequest request, User user) {
        // Generate unique order code (timestamp-based)
        Long orderCode = generateOrderCode();

        // Create order
        Order order = Order.builder()
                .orderCode(orderCode)
                .user(user)
                .status(OrderStatus.PENDING)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        // Set shipping address
        if (request.getShippingAddressId() != null) {
            Address address = addressRepository.findById(request.getShippingAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Địa chỉ không tồn tại"));
            if (!address.getUser().getId().equals(user.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Không có quyền sử dụng địa chỉ này");
            }
            order.setShippingAddress(address);
        } else {
            // Use default address
            Address defaultAddress = user.getAddresses().stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElse(user.getAddresses().isEmpty() ? null : user.getAddresses().get(0));
            order.setShippingAddress(defaultAddress);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        // Create order items
        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Sản phẩm không tồn tại"));

            // Validate stock
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new AppException(ErrorCode.BAD_REQUEST, 
                    String.format("Sản phẩm %s chỉ còn %d sản phẩm", product.getName(), product.getStock()));
            }

            // Validate price
            BigDecimal expectedPrice = product.getPrice();
            if (product.getPromotion() != null) {
                // Calculate discounted price
                BigDecimal discountPercent = BigDecimal.valueOf(product.getPromotion().getDiscountPercent());
                BigDecimal discount = expectedPrice.multiply(discountPercent).divide(BigDecimal.valueOf(100));
                expectedPrice = expectedPrice.subtract(discount);
            }

            if (itemRequest.getUnitPrice().compareTo(expectedPrice) != 0) {
                log.warn("Price mismatch for product {}: expected {}, got {}", 
                    product.getId(), expectedPrice, itemRequest.getUnitPrice());
            }

            // Calculate item total
            BigDecimal itemTotal = itemRequest.getUnitPrice()
                    .subtract(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO)
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .discountAmount(itemRequest.getDiscountAmount() != null ? itemRequest.getDiscountAmount() : BigDecimal.ZERO)
                    .totalPrice(itemTotal)
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(itemTotal);
            totalDiscount = totalDiscount.add(
                itemRequest.getDiscountAmount() != null ? 
                itemRequest.getDiscountAmount().multiply(BigDecimal.valueOf(itemRequest.getQuantity())) : 
                BigDecimal.ZERO
            );
        }

        // Set order amounts
        order.setSubtotal(subtotal);
        order.setDiscountAmount(totalDiscount);
        order.setShippingFee(BigDecimal.ZERO); // TODO: Calculate shipping fee
        order.setTotalAmount(subtotal.subtract(totalDiscount).add(order.getShippingFee()));

        // Save order
        Order savedOrder = orderRepository.save(order);
        log.info("Created order: {} for user: {}", savedOrder.getOrderCode(), user.getEmail());

        return savedOrder;
    }

    @Override
    @Transactional
    public Order confirmOrder(Long orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng không tồn tại"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng đã được xử lý");
        }

        // Deduct stock
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new AppException(ErrorCode.BAD_REQUEST, 
                    String.format("Sản phẩm %s không đủ tồn kho", product.getName()));
            }
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // Update order status
        order.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(order);
        log.info("Confirmed order: {}", orderCode);

        return confirmedOrder;
    }

    @Override
    public Order getOrderByCode(Long orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng không tồn tại"));
    }

    /**
     * Generate unique order code (timestamp-based)
     * Format: timestamp in milliseconds (last 10 digits)
     */
    private Long generateOrderCode() {
        long timestamp = Instant.now().toEpochMilli();
        // Use last 10 digits to avoid overflow
        return timestamp % 10000000000L;
    }
}

