package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.PaymentRequest;
import iuh.fit.se.ecommerce.dto.request.PayOSWebhookRequest;
import iuh.fit.se.ecommerce.dto.response.PaymentResponse;
import iuh.fit.se.ecommerce.service.interfaces.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentRequest request) {
        
        PaymentResponse response = paymentService.createPayment(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payos-callback")
    public ResponseEntity<Map<String, String>> payOSCallback(@RequestBody PayOSWebhookRequest webhook) {
        log.info("Received PayOS webhook callback");
        paymentService.handlePayOSCallback(webhook);
        return ResponseEntity.ok(Map.of("code", "00", "desc", "success"));
    }

    @GetMapping("/status/{orderCode}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long orderCode) {
        PaymentResponse response = paymentService.getPaymentStatus(orderCode);
        return ResponseEntity.ok(response);
    }
}

