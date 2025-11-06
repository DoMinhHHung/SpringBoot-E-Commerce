package iuh.fit.se.ecommerce.dto.response;

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
    private List<Suggestion> suggestions; // changed to Suggestion objects

    // keep a convenient 2-arg constructor
    public ChatResponse(String text, List<SimpleProduct> products) {
        this.text = text;
        this.products = products;
        this.suggestions = List.of();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleProduct {
        private Long id;
        private String name;
        private String imageUrl;
        private BigDecimal price;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Suggestion {
        private String label; // what to show on the button
        private String query; // actionable query sent back when clicked
    }
}
