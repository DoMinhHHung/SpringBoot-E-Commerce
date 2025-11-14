package iuh.fit.se.ecommerce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSWebhookRequest {
    private String code;
    private String desc;
    private PayOSWebhookData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayOSWebhookData {
        private String orderCode;
        private Integer amount;
        private String description;
        private String accountNumber;
        private String reference;
        private String transactionDateTime;
        private String currency;
        private String paymentLinkId;
        private String code;
        private String desc;
        private Integer counterAccountBankId;
        private String counterAccountBankName;
        private String counterAccountName;
        private String counterAccountNumber;
        private Integer virtualAccountName;
        private String virtualAccountNumber;
    }
}

