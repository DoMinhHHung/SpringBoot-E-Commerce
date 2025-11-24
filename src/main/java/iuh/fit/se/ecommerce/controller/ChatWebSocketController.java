package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ChatMessage;
import iuh.fit.se.ecommerce.dto.response.ChatResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.service.interfaces.GeminiService;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import iuh.fit.se.ecommerce.config.SupportSessionRegistry;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;
    private final GeminiService geminiService;
    private final SupportSessionRegistry registry;

    private static final String GREETING_RESPONSE = "Xin chào! Tôi là trợ lý AI, chuyên hỗ trợ bạn tìm kiếm các sản phẩm Laptop và Phụ kiện. Bạn cần tìm sản phẩm gì ạ? Hãy mô tả yêu cầu của bạn!";
    private static final String CONTEXT_QUESTION_RESPONSE = "Bạn đang muốn hỏi về sản phẩm nào? Vui lòng cung cấp thêm thông tin chi tiết (tên model, mã sản phẩm...) hoặc cho tôi biết bạn đang xem sản phẩm nào để tôi hỗ trợ chính xác nhất.";
    private static final String NOT_FOUND_RESPONSE = "Xin lỗi, tôi chưa tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn. Bạn có thể thử tìm với từ khóa khác hoặc chọn một trong các gợi ý dưới đây nhé:";
    private static final String GEMINI_ERROR_RESPONSE = "Xin lỗi, tôi đang bận xử lý dữ liệu. Bạn có thể thử lại sau giây lát hoặc nhập lại từ khóa đơn giản hơn nhé!";

    private static final List<ChatResponse.Suggestion> DEFAULT_SUGGESTIONS = List.of(
            new ChatResponse.Suggestion("Laptop Dell", "QUERY: laptop Dell"),
            new ChatResponse.Suggestion("Laptop Gaming", "QUERY: laptop Gaming"),
            new ChatResponse.Suggestion("Giá 15-20 triệu", "QUERY: price:15000000-20000000"),
            new ChatResponse.Suggestion("Phụ kiện Khuyến mãi", "QUERY: phụ kiện khuyến mãi"),
            new ChatResponse.Suggestion("Tìm chuột không dây", "QUERY: chuột không dây")
    );


    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessage message) {
        // Handle special CALL_HUMAN request: register pending support and notify admins
        if (message != null && message.getText() != null && "CALL_HUMAN".equalsIgnoreCase(message.getText().trim())) {
            try {
                String sid = message.getSessionId();
                registry.registerPending(sid, null, message.getProductId());
                // notify the user that request was received
                messagingTemplate.convertAndSend("/topic/replies." + sid,
                        new ChatResponse("Yêu cầu liên hệ kỹ thuật đã được ghi nhận. Chúng tôi sẽ chuyển tới kỹ thuật viên.", java.util.List.of(), java.util.List.of()));
            } catch (Exception ex) {
                // best-effort: log to console (avoid failing the whole flow)
                System.err.println("Failed to register CALL_HUMAN request: " + ex.getMessage());
            }
            return;
        }
        // If this session is already assigned to an admin, forward user's messages to admin
        try {
            String sid = message == null ? null : message.getSessionId();
            String raw = message == null || message.getText() == null ? "" : message.getText();
            if (sid != null && registry.isAssigned(sid)) {
                // ignore empty initial greeting when assigned
                if (raw.trim().isEmpty()) return;
                Map<String, Object> payload = new HashMap<>();
                payload.put("sessionId", sid);
                payload.put("adminId", null);
                payload.put("text", raw);
                payload.put("timestamp", Instant.now().toEpochMilli());
                messagingTemplate.convertAndSend("/topic/admin.session." + sid, payload);
                return;
            }
        } catch (Exception ex) {
            System.err.println("Error forwarding message to admin: " + ex.getMessage());
        }
        ChatResponse response;

        String rawText = message.getText();
        String queryText = rawText.trim();
        String finalQuery = null;
        String botText = null;

        if (queryText.equalsIgnoreCase("brand:ALL")) {
            queryText = resolveBrandQuery(message);
        } else if (queryText.toUpperCase().startsWith("QUERY:") || queryText.toUpperCase().startsWith("BRAND:") || queryText.toUpperCase().startsWith("TYPE:") || queryText.toUpperCase().startsWith("PRICE:") || queryText.toUpperCase().startsWith("PROMOTION:")) {
            finalQuery = queryText;
        } else {
            String geminiResponse = geminiService.chat(queryText);

            if (geminiResponse == null) {
                botText = GEMINI_ERROR_RESPONSE;
                response = new ChatResponse(botText, List.of(), DEFAULT_SUGGESTIONS);
                messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
                return;
            }

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
                finalQuery = geminiResponse.substring("QUERY:".length()).trim();
            } else {
                finalQuery = geminiResponse.trim();
            }
        }

        if (finalQuery == null) {
            finalQuery = rawText.trim();
        }
        List<ProductResponse> products = productService.findByQuery(finalQuery);

        if (products.isEmpty()) {
            botText = NOT_FOUND_RESPONSE;
            response = new ChatResponse(botText, List.of(), DEFAULT_SUGGESTIONS);
        } else {
            botText = "Dưới đây là các sản phẩm phù hợp với yêu cầu của bạn về **" + finalQuery + "**:";
            List<ChatResponse.SimpleProduct> simpleProducts = products.stream()
                    .map(p -> new ChatResponse.SimpleProduct(
                            p.getId(),
                            p.getName(),
                            p.getMainImage(),
                            p.getPriceAfterDiscount() != null ? p.getPriceAfterDiscount() : p.getPrice()
                    ))
                    .collect(Collectors.toList());

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