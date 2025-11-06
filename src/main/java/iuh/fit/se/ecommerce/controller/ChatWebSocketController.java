package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ChatMessage;
import iuh.fit.se.ecommerce.dto.response.ChatResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.service.interfaces.GeminiService;
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
    private final GeminiService geminiService;

    private static final String GREETING_RESPONSE = "Xin chào! Tôi là trợ lý AI, chuyên hỗ trợ bạn tìm kiếm các sản phẩm Laptop và Phụ kiện. Bạn cần tìm sản phẩm gì ạ? Hãy mô tả yêu cầu của bạn!";
    private static final String CONTEXT_QUESTION_RESPONSE = "Bạn đang muốn hỏi về sản phẩm nào? Vui lòng cung cấp thêm thông tin chi tiết (tên model, mã sản phẩm...) hoặc cho tôi biết bạn đang xem sản phẩm nào để tôi hỗ trợ chính xác nhất.";
    private static final String NOT_FOUND_RESPONSE = "Xin lỗi, tôi chưa tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn. Bạn có thể thử tìm với từ khóa khác hoặc chọn một trong các gợi ý dưới đây nhé:";
    private static final String GEMINI_ERROR_RESPONSE = "Xin lỗi, tôi đang bận xử lý dữ liệu. Bạn có thể thử lại sau giây lát hoặc nhập lại từ khóa đơn giản hơn nhé!";

    // Các gợi ý mặc định (dùng khi chào hỏi, không tìm thấy, hoặc Gemini lỗi)
    private static final List<ChatResponse.Suggestion> DEFAULT_SUGGESTIONS = List.of(
            new ChatResponse.Suggestion("Laptop Dell", "QUERY: laptop Dell"),
            new ChatResponse.Suggestion("Laptop Gaming", "QUERY: laptop Gaming"),
            new ChatResponse.Suggestion("Giá 15-20 triệu", "QUERY: price:15000000-20000000"),
            new ChatResponse.Suggestion("Phụ kiện Khuyến mãi", "QUERY: phụ kiện khuyến mãi"),
            new ChatResponse.Suggestion("Tìm chuột không dây", "QUERY: chuột không dây")
    );


    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessage message) {
        ChatResponse response;

        // 1. Lấy text gốc từ người dùng
        String rawText = message.getText();
        String queryText = rawText.trim();
        String finalQuery = null;
        String botText = null;

        // Xử lý các suggestion query đã được mã hóa sẵn (ví dụ: brand:ALL)
        // Logic này cần giữ lại để xử lý button click (nếu có)
        if (queryText.equalsIgnoreCase("brand:ALL")) {
            queryText = resolveBrandQuery(message); // Helper này sẽ lấy brand của sản phẩm đang xem
        } else if (queryText.toUpperCase().startsWith("QUERY:") || queryText.toUpperCase().startsWith("BRAND:") || queryText.toUpperCase().startsWith("TYPE:") || queryText.toUpperCase().startsWith("PRICE:") || queryText.toUpperCase().startsWith("PROMOTION:")) {
            // Nếu input đã là một structured query (từ nút gợi ý), ta dùng nó luôn
            finalQuery = queryText;
        } else {
            // 2. Gọi Gemini để phân tích ý định
            String geminiResponse = geminiService.chat(queryText);

            if (geminiResponse == null) {
                // Xử lý lỗi API hoặc Timeout
                botText = GEMINI_ERROR_RESPONSE;
                response = new ChatResponse(botText, List.of(), DEFAULT_SUGGESTIONS);
                messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
                return;
            }

            // 3. Phân tích phản hồi của Gemini
            String upperResponse = geminiResponse.trim().toUpperCase();

            if (upperResponse.startsWith("GREETING")) {
                botText = GREETING_RESPONSE;
                response = new ChatResponse(botText, List.of(), DEFAULT_SUGGESTIONS);
                messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
                return;
            } else if (upperResponse.startsWith("CONTEXT_QUESTION")) {
                botText = CONTEXT_QUESTION_RESPONSE;
                response = new ChatResponse(botText, List.of(), DEFAULT_SUGGESTIONS);
                messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
                return;
            } else if (upperResponse.startsWith("QUERY:")) {
                // Phản hồi là Query tìm kiếm -> Lấy Query
                finalQuery = geminiResponse.substring("QUERY:".length()).trim();
            } else {
                // Fallback: Gemini trả về text không theo format, coi là QUERY free-text
                finalQuery = geminiResponse.trim();
            }
        }

        // 4. Nếu chưa có finalQuery (hoặc là free text không cấu trúc), ta sẽ thử dùng normalizeSuggestionText
        // để hỗ trợ các cú pháp giá tiền phức tạp (ví dụ: 10-15 triệu).
        if (finalQuery == null || (!finalQuery.toUpperCase().startsWith("QUERY:") && !finalQuery.toUpperCase().startsWith("BRAND:") && !finalQuery.toUpperCase().startsWith("TYPE:"))) {
            // Chạy qua parser cũ để bắt các cú pháp giá tiền "triệu"
            String normalizedPriceQuery = normalizeSuggestionText(finalQuery == null ? rawText : finalQuery);
            if (normalizedPriceQuery.toUpperCase().startsWith("PRICE:")) {
                finalQuery = normalizedPriceQuery; // Ưu tiên query giá tiền đã được chuẩn hóa
            } else if (finalQuery == null) {
                finalQuery = rawText.trim(); // Nếu vẫn null, dùng raw text
            }
        }

        // --- Bắt đầu tìm kiếm sản phẩm ---
        List<ProductResponse> products = productService.findByQuery(finalQuery);

        if (products.isEmpty()) {
            // Không tìm thấy sản phẩm
            botText = NOT_FOUND_RESPONSE;
            response = new ChatResponse(botText, List.of(), DEFAULT_SUGGESTIONS);
        } else {
            // Tìm thấy sản phẩm
            botText = "Dưới đây là các sản phẩm phù hợp với yêu cầu của bạn về **" + finalQuery + "**:";
            List<ChatResponse.SimpleProduct> simpleProducts = products.stream()
                    .map(p -> new ChatResponse.SimpleProduct(
                            p.getId(),
                            p.getName(),
                            p.getMainImage(),
                            p.getPriceAfterDiscount() != null ? p.getPriceAfterDiscount() : p.getPrice()
                    ))
                    .collect(Collectors.toList());

            // Gợi ý cho kết quả tìm kiếm
            List<ChatResponse.Suggestion> suggestions = List.of(
                    new ChatResponse.Suggestion("Xem tất cả sản phẩm cùng hãng", resolveBrandQuery(message)),
                    new ChatResponse.Suggestion("Lọc kết quả theo tầm giá", "price:15000000-25000000"),
                    new ChatResponse.Suggestion("Xem phụ kiện liên quan", "type:ACCESSORY"),
                    new ChatResponse.Suggestion("Xem chi tiết sản phẩm", "")
            );

            response = new ChatResponse(botText, simpleProducts, suggestions);
        }

        messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
    }

    // --- HELPER METHODS (Giữ lại logic cũ để xử lý cú pháp giá/brand) ---

    // Try to normalize a human-readable suggestion label into a structured query understood by findByQuery
    private String normalizeSuggestionText(String raw) {
        if (raw == null) return "";
        String lower = raw.trim().toLowerCase();
        if (lower.isEmpty()) return "";

        // Nếu đã là structured (từ nút gợi ý), trả về as-is
        if (lower.startsWith("brand:") || lower.startsWith("type:") || lower.startsWith("price:") || lower.startsWith("spec:") || lower.startsWith("promotion:") || lower.startsWith("query:")) {
            return raw.trim();
        }

        // Logic cũ: Try extract brand after patterns like "tìm theo hãng: Dell" or "hãng: Dell"
        Pattern brandPattern = Pattern.compile("hãng[:\\s]*([\\p{L}0-9\\s-]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher mBrand = brandPattern.matcher(raw);
        if (mBrand.find()) {
            String brand = mBrand.group(1).trim();
            return "brand:" + brand;
        }

        // Logic cũ: Try extract type after "loại: ..." or "tìm theo loại: ..."
        Pattern typePattern = Pattern.compile("loại[:\\s]*([\\p{L}0-9\\s-]+)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher mType = typePattern.matcher(raw);
        if (mType.find()) {
            String type = mType.group(1).trim();
            return "type:" + type;
        }

        // Logic cũ: Try detect price ranges like "10-15 triệu"
        Pattern rangePattern = Pattern.compile("(\\d+(?:[\\.,]?\\d+)?)\\s*-\\s*(\\d+(?:[\\.,]?\\d+)?)\\s*(triệu|m|vnđ|vnd)?", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
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

        // Logic cũ: Try single number with 'triệu' e.g., "10 triệu" -> search around that value +/-20%
        Pattern singlePrice = Pattern.compile("(\\d+(?:[\\.,]?\\d+)?)\\s*(triệu|m|vnđ|vnd)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
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

        // Fallback: return raw (free-text).
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