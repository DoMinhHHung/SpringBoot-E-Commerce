package iuh.fit.se.ecommerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayOSGateway {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.api-url}")
    private String apiUrl;

    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Data
    public static class PayOSCreateRequest {
        private Long orderCode;
        private Long amount;
        private String description;
        private String returnUrl;
        private String cancelUrl;
    }

    @Data
    public static class PayOSResponse {
        private String code;
        private String desc;
        private PayOSData data;

        @Data
        public static class PayOSData {
            private Long orderCode;
            private Long amount;
            private String description;
            private String accountNumber;
            private String accountName;
            private String qrCode;
            private String checkoutUrl;
            private String paymentLinkId;
        }
    }

    public PayOSResponse createPaymentLink(Long orderCode, Long amount, String description) {
        try {
            PayOSCreateRequest request = new PayOSCreateRequest();
            request.setOrderCode(orderCode);
            request.setAmount(amount);
            request.setDescription(description);
            request.setReturnUrl(returnUrl);
            request.setCancelUrl(cancelUrl);

            // Tạo checksum
            String checksum = createChecksum(request);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", clientId);
            headers.set("x-api-key", apiKey);

            // Body với checksum
            Map<String, Object> body = new HashMap<>();
            body.put("orderCode", request.getOrderCode());
            body.put("amount", request.getAmount());
            body.put("description", request.getDescription());
            body.put("returnUrl", request.getReturnUrl());
            body.put("cancelUrl", request.getCancelUrl());
            body.put("signature", checksum);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            log.info("Calling PayOS API: {}", apiUrl + "/payment-requests");
            ResponseEntity<PayOSResponse> response = restTemplate.exchange(
                    apiUrl + "/v2/payment-requests",
                    HttpMethod.POST,
                    entity,
                    PayOSResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                PayOSResponse payOSResponse = response.getBody();
                if ("00".equals(payOSResponse.getCode())) {
                    log.info("PayOS payment link created successfully: {}", payOSResponse.getData().getPaymentLinkId());
                    return payOSResponse;
                } else {
                    log.error("PayOS API error: {} - {}", payOSResponse.getCode(), payOSResponse.getDesc());
                    throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi tạo payment link: " + payOSResponse.getDesc());
                }
            }

            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi kết nối PayOS API");

        } catch (Exception e) {
            log.error("Error creating PayOS payment link: ", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi tạo payment link: " + e.getMessage());
        }
    }

    public boolean verifyWebhook(String data, String signature) {
        try {
            String calculatedChecksum = createChecksumFromString(data);
            return calculatedChecksum.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook: ", e);
            return false;
        }
    }

    private String createChecksum(PayOSCreateRequest request) {
        try {
            String data = String.format(
                    "amount=%d&cancelUrl=%s&description=%s&orderCode=%d&returnUrl=%s",
                    request.getAmount(),
                    request.getCancelUrl(),
                    request.getDescription(),
                    request.getOrderCode(),
                    request.getReturnUrl()
            );
            return createChecksumFromString(data);
        } catch (Exception e) {
            log.error("Error creating checksum: ", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi tạo checksum");
        }
    }

    private String createChecksumFromString(String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            log.info("Checksum key: {}", checksumKey);
            SecretKeySpec secretKey = new SecretKeySpec(
                    checksumKey.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error creating checksum: ", e);
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Lỗi tạo checksum");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

