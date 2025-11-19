package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.OrderUpdateRequest;
import iuh.fit.se.ecommerce.dto.response.OrderDetailResponse;
import iuh.fit.se.ecommerce.dto.response.OrderResponse;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import iuh.fit.se.ecommerce.service.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAllOrders(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderCode) {
        OrderDetailResponse orderDetail = orderService.getOrderDetailForAdmin(orderCode);
        return ResponseEntity.ok(orderDetail);
    }

    @PutMapping("/{orderCode}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderCode,
            @Valid @RequestBody OrderUpdateRequest request
    ) {
        OrderResponse updated = orderService.updateOrderStatus(
                orderCode, 
                request.getStatus(), 
                request.getNotes()
        );
        return ResponseEntity.ok(updated);
    }
}

