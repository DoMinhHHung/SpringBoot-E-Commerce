package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.ChatMessage;
import iuh.fit.se.ecommerce.dto.ChatResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessage message) {
        ChatResponse response;

        String rawText = message.getText();
        String queryText = normalizeSuggestionText(rawText);

        if (queryText != null && queryText.trim().equalsIgnoreCase("brand:ALL")) {
            Long pid = message.getProductId();
            if (pid != null) {
                try {
                    ProductDetailResponse pd = productService.getProductById(pid);
                    if (pd != null && pd.getBrand() != null && !pd.getBrand().isBlank()) {
                        queryText = "brand:" + pd.getBrand();
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (queryText == null || queryText.trim().isEmpty()) {
            List<ChatResponse.Suggestion> suggestions = List.of(
                    new ChatResponse.Suggestion("Tìm theo hãng: Dell", "brand:Dell"),
                    new ChatResponse.Suggestion("Tìm theo hãng: HP", "brand:HP"),
                    new ChatResponse.Suggestion("Tìm theo loại: Gaming", "type:Gaming"),
                    new ChatResponse.Suggestion("Tầm giá: 10-15 triệu", "price:10000000-15000000"),
                    new ChatResponse.Suggestion("Phụ kiện đang khuyến mãi", "promotion:accessories")
            );

            response = new ChatResponse(
                    "Xin chào! Tôi có thể giúp gì cho bạn? Hãy nhập từ khóa sản phẩm bạn muốn tìm kiếm.",
                    List.of(),
                    suggestions
            );
        } else {
            List<ProductResponse> products = productService.findByQuery(queryText);

            if (products.isEmpty()) {
                List<ChatResponse.Suggestion> suggestions = List.of(
                        new ChatResponse.Suggestion("Tìm theo hãng: Dell", "brand:Dell"),
                        new ChatResponse.Suggestion("Tìm theo loại: Gaming", "type:Gaming"),
                        new ChatResponse.Suggestion("Tầm giá: 10-15 triệu", "price:10000000-15000000"),
                        new ChatResponse.Suggestion("Tìm theo tên model (ví dụ: XPS 13)", ""),
                        new ChatResponse.Suggestion("Xem phụ kiện đang khuyến mãi", "promotion:accessories")
                );

                response = new ChatResponse(
                        "Xin lỗi, tôi không tìm thấy sản phẩm phù hợp với \"" + rawText + "\". Bạn có thể thử với từ khóa khác không?",
                        List.of(),
                        suggestions
                );
            } else {
                List<ChatResponse.SimpleProduct> simpleProducts = products.stream()
                        .map(p -> new ChatResponse.SimpleProduct(
                                p.getId(),
                                p.getName(),
                                p.getMainImage(),
                                p.getPriceAfterDiscount() != null ? p.getPriceAfterDiscount() : p.getPrice()
                        ))
                        .collect(Collectors.toList());

                List<ChatResponse.Suggestion> suggestions = List.of(
                        // If productId is available, resolve brand and include it directly in the suggestion
                        new ChatResponse.Suggestion("Xem tất cả sản phẩm cùng hãng", resolveBrandQuery(message)),
                        new ChatResponse.Suggestion("Lọc kết quả theo tầm giá", "price:10000000-15000000"),
                        new ChatResponse.Suggestion("Xem phụ kiện liên quan", "type:ACCESSORY"),
                        new ChatResponse.Suggestion("Xem chi tiết sản phẩm", "")
                );

                response = new ChatResponse(
                        "Dưới đây là một số sản phẩm phù hợp với tìm kiếm của bạn:",
                        simpleProducts,
                        suggestions
                );
            }
        }

        messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
    }

    // Try to normalize a human-readable suggestion label into a structured query understood by findByQuery
    private String normalizeSuggestionText(String raw) {
        if (raw == null) return "";
        String lower = raw.trim().toLowerCase();
        if (lower.isEmpty()) return "";

        // If already structured (brand:, type:, price:, spec:, promotion:), return as-is
        if (lower.startsWith("brand:") || lower.startsWith("type:") || lower.startsWith("price:") || lower.startsWith("spec:") || lower.startsWith("promotion:")) {
            return raw.trim();
        }

        // Try extract brand after patterns like "tìm theo hãng: Dell" or "hãng: Dell"
        Pattern brandPattern = Pattern.compile("hãng[:\\s]*([\\p{L}0-9\\s-]+)", Pattern.CASE_INSENSITIVE);
        Matcher mBrand = brandPattern.matcher(raw);
        if (mBrand.find()) {
            String brand = mBrand.group(1).trim();
            return "brand:" + brand;
        }

        // Try extract type after "loại: ..." or "tìm theo loại: ..."
        Pattern typePattern = Pattern.compile("loại[:\\s]*([\\p{L}0-9\\s-]+)", Pattern.CASE_INSENSITIVE);
        Matcher mType = typePattern.matcher(raw);
        if (mType.find()) {
            String type = mType.group(1).trim();
            return "type:" + type;
        }

        // Try detect price ranges like "10-15 triệu", "10000000-15000000" or "10 triệu"
        Pattern rangePattern = Pattern.compile("(\\d+(?:[\\.,]?\\d+)?)\\s*-\\s*(\\d+(?:[\\.,]?\\d+)?)\\s*(triệu|m|vnđ|vnd)?", Pattern.CASE_INSENSITIVE);
        Matcher mRange = rangePattern.matcher(raw);
        if (mRange.find()) {
            try {
                String a = mRange.group(1).replaceAll("\\.", "").replaceAll(",", "");
                String b = mRange.group(2).replaceAll("\\.", "").replaceAll(",", "");
                String unit = mRange.group(3);
                java.math.BigDecimal min = new java.math.BigDecimal(a);
                java.math.BigDecimal max = new java.math.BigDecimal(b);
                if (unit != null && unit.toLowerCase().contains("triệu")) {
                    min = min.multiply(java.math.BigDecimal.valueOf(1_000_000L));
                    max = max.multiply(java.math.BigDecimal.valueOf(1_000_000L));
                }
                return "price:" + min.toPlainString() + "-" + max.toPlainString();
            } catch (Exception ignored) {}
        }

        // Try single number with 'triệu' e.g., "10 triệu" -> search around that value +/-20%
        Pattern singlePrice = Pattern.compile("(\\d+(?:[\\.,]?\\d+)?)\\s*(triệu|m|vnđ|vnd)", Pattern.CASE_INSENSITIVE);
        Matcher mSingle = singlePrice.matcher(raw);
        if (mSingle.find()) {
            try {
                String a = mSingle.group(1).replaceAll("\\.", "").replaceAll(",", "");
                java.math.BigDecimal val = new java.math.BigDecimal(a);
                val = val.multiply(java.math.BigDecimal.valueOf(1_000_000L));
                java.math.BigDecimal min = val.multiply(java.math.BigDecimal.valueOf(8)).divide(java.math.BigDecimal.valueOf(10));
                java.math.BigDecimal max = val.multiply(java.math.BigDecimal.valueOf(12)).divide(java.math.BigDecimal.valueOf(10));
                return "price:" + min.toPlainString() + "-" + max.toPlainString();
            } catch (Exception ignored) {}
        }

        // Try detect spec keywords inside parentheses like "(ví dụ: Gaming, Ultrabook)" -> ignore and return raw for free-text
        // Fallback: return raw (free-text) so findByQuery will try free-text search including specs
        return raw.trim();
    }

    // Helper to return "brand:<brand>" when productId is present and brand is known, otherwise "brand:ALL"
    private String resolveBrandQuery(ChatMessage message) {
        if (message == null) return "brand:ALL";
        Long pid = message.getProductId();
        if (pid == null) return "brand:ALL";
        try {
            ProductDetailResponse pd = productService.getProductById(pid);
            if (pd != null && pd.getBrand() != null && !pd.getBrand().isBlank()) {
                return "brand:" + pd.getBrand();
            }
        } catch (Exception ignored) {}
        return "brand:ALL";
    }
}
