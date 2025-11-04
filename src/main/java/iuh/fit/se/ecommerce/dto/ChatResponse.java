package iuh.fit.se.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String text;
    private List<SimpleProduct> products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleProduct {
        private Long id;
        private String name;
        private String imageUrl;
        private BigDecimal price;
    }
}
