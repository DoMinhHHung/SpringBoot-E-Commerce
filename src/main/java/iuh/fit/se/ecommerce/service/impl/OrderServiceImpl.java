package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.dto.response.AddressResponse;
import iuh.fit.se.ecommerce.dto.response.OrderDetailResponse;
import iuh.fit.se.ecommerce.dto.response.OrderItemResponse;
import iuh.fit.se.ecommerce.dto.response.OrderResponse;
import iuh.fit.se.ecommerce.entity.*;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import iuh.fit.se.ecommerce.entity.enums.PaymentStatus;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.*;
import iuh.fit.se.ecommerce.service.interfaces.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

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

    @Override
    public Page<OrderResponse> getUserOrders(String userEmail, OrderStatus status, String search, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Page<Order> orders;
        
        // If search query is provided
        if (search != null && !search.trim().isEmpty()) {
            if (status != null) {
                // Search with status filter
                orders = orderRepository.searchUserOrdersByStatus(user, status, search.trim(), pageable);
            } else {
                // Search without status filter
                orders = orderRepository.searchUserOrders(user, search.trim(), pageable);
            }
        } else {
            // No search - existing logic
            if (status != null) {
                orders = orderRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable);
            } else {
                orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            }
        }

        return orders.map(this::mapToOrderResponse);
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long orderCode, String userEmail) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng không tồn tại"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check if user owns this order
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Không có quyền xem đơn hàng này");
        }

        return mapToOrderDetailResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return orders.map(this::mapToOrderResponse);
    }

    @Override
    public OrderDetailResponse getOrderDetailForAdmin(Long orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng không tồn tại"));

        return mapToOrderDetailResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderCode, OrderStatus newStatus, String notes) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Đơn hàng không tồn tại"));

        OrderStatus currentStatus = order.getStatus();

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);

        // Update status
        order.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            String currentNotes = order.getNotes() != null ? order.getNotes() : "";
            order.setNotes(currentNotes + (currentNotes.isEmpty() ? "" : "\n") + 
                    String.format("[%s] %s: %s", LocalDateTime.now(), getStatusLabel(newStatus), notes));
        }

        Order updatedOrder = orderRepository.save(order);
        log.info("Updated order {} status from {} to {}", orderCode, currentStatus, newStatus);

        return mapToOrderResponse(updatedOrder);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        // Allow transitions
        boolean isValid = switch (current) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELLED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED, REFUNDED -> false; // Cannot change from these statuses
        };

        if (!isValid) {
            throw new AppException(ErrorCode.BAD_REQUEST, 
                    String.format("Không thể chuyển đơn hàng từ trạng thái '%s' sang '%s'", 
                            getStatusLabel(current), getStatusLabel(newStatus)));
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        // Map items
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImage(item.getProduct().getMainImage())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountAmount(item.getDiscountAmount())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusLabel(getStatusLabel(order.getStatus()))
                .totalAmount(order.getTotalAmount())
                .itemCount(order.getItems() != null ? order.getItems().size() : 0)
                .userName(order.getUser().getFullName())
                .userEmail(order.getUser().getEmail())
                .items(items)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderDetailResponse mapToOrderDetailResponse(Order order) {
        // Map items
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImage(item.getProduct().getMainImage())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountAmount(item.getDiscountAmount())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        // Map address
        AddressResponse addressResponse = null;
        if (order.getShippingAddress() != null) {
            Address addr = order.getShippingAddress();
            addressResponse = AddressResponse.builder()
                    .id(addr.getId())
                    .receiverName(addr.getReceiverName())
                    .receiverPhone(addr.getReceiverPhone())
                    .receiverEmail(null) // Address entity doesn't have email
                    .country("Vietnam")
                    .province(addr.getProvince())
                    .district(addr.getDistrict())
                    .ward(addr.getWard())
                    .addressDetail(addr.getDetail())
                    .isDefault(addr.isDefault())
                    .build();
        }

        // Map payment info
        OrderDetailResponse.PaymentInfo paymentInfo = null;
        var paymentOpt = paymentRepository.findByOrder(order);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            paymentInfo = OrderDetailResponse.PaymentInfo.builder()
                    .paymentMethod(payment.getMethod() != null ? payment.getMethod().name() : null)
                    .paymentStatus(payment.getStatus() != null ? payment.getStatus().name() : null)
                    .transactionId(payment.getTransactionId())
                    .build();
        }

        // Get timestamps based on status
        LocalDateTime confirmedAt = order.getStatus().ordinal() >= OrderStatus.CONFIRMED.ordinal() 
                ? order.getUpdatedAt() : null;
        LocalDateTime shippedAt = order.getStatus().ordinal() >= OrderStatus.SHIPPED.ordinal() 
                ? order.getUpdatedAt() : null;
        LocalDateTime deliveredAt = order.getStatus() == OrderStatus.DELIVERED 
                ? order.getUpdatedAt() : null;

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .statusLabel(getStatusLabel(order.getStatus()))
                .progressStep(getProgressStep(order.getStatus()))
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .shippingFee(order.getShippingFee())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .shippingAddress(addressResponse)
                .items(items)
                .paymentInfo(paymentInfo)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .confirmedAt(confirmedAt)
                .shippedAt(shippedAt)
                .deliveredAt(deliveredAt)
                .build();
    }

    private int getProgressStep(OrderStatus status) {
        return switch (status) {
            case PENDING -> 0;      // Đang chờ xác nhận
            case CONFIRMED -> 1;   // Đã xác nhận
            case PROCESSING, SHIPPED -> 2; // Đang vận chuyển
            case DELIVERED -> 3;   // Đã nhận hàng
            default -> 0;          // CANCELLED, REFUNDED
        };
    }

    private String getStatusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Đang chờ xác nhận đơn hàng";
            case CONFIRMED -> "Đã xác nhận";
            case PROCESSING -> "Đang xử lý";
            case SHIPPED -> "Đang vận chuyển";
            case DELIVERED -> "Đã nhận hàng";
            case CANCELLED -> "Đã hủy";
            case REFUNDED -> "Đã hoàn tiền";
        };
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

