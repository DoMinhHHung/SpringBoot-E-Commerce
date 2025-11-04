package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.ChatMessage;
import iuh.fit.se.ecommerce.dto.ChatResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ProductService productService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat")
    public void handleChatMessage(ChatMessage message) {
        ChatResponse response;

        if (message.getText() == null || message.getText().trim().isEmpty()) {
            response = new ChatResponse(
                    "Xin chào! Tôi có thể giúp gì cho bạn? Hãy nhập từ khóa sản phẩm bạn đang tìm kiếm.",
                    List.of()
            );
        } else {
            List<ProductResponse> products = productService.findByQuery(message.getText());
            
            if (products.isEmpty()) {
                response = new ChatResponse(
                        "Xin lỗi, tôi không tìm thấy sản phẩm phù hợp với \"" + message.getText() + 
                        "\". Bạn có thể thử với từ khóa khác không?",
                        List.of()
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
                
                response = new ChatResponse(
                        "Dưới đây là một số sản phẩm phù hợp với tìm kiếm của bạn:",
                        simpleProducts
                );
            }
        }

        messagingTemplate.convertAndSend("/topic/replies." + message.getSessionId(), response);
    }
}
