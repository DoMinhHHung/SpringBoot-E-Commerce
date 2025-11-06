package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.service.interfaces.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * GeminiServiceImpl - gọi Google Generative Language API.
 * Đã cập nhật để dùng chuẩn Gemini API (action: generateContent và request body chuẩn)
 * và nhúng Prompt Engineering (system instruction, few-shot examples) để ép AI trả về format cố định.
 */
@Service
public class GeminiServiceImpl implements GeminiService {
    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final String action;
    private final Duration timeout;
    private final String systemInstruction; // Thêm
    private final String fewshotExamples;     // Thêm
    private final Logger logger = LoggerFactory.getLogger(GeminiServiceImpl.class);

    public GeminiServiceImpl(WebClient geminiWebClient,
                             @Value("${gemini.api.key:}") String apiKey,
                             @Value("${generative.model:gemini-2.5-flash}") String model,
                             @Value("${generative.api.action:generateContent}") String action,
                             @Value("${ai.parser.timeout.ms:4000}") long timeoutMs,
                             // Thêm Injection cho Prompt từ application.properties
                             @Value("${gemini.system.instruction}") String systemInstruction,
                             @Value("${gemini.fewshot.examples}") String fewshotExamples) {
        this.webClient = geminiWebClient;
        this.apiKey = apiKey;
        this.model = model;
        this.action = action;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.systemInstruction = systemInstruction; // Gán
        this.fewshotExamples = fewshotExamples;     // Gán
    }

    @Override
    public String chat(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.warn("Gemini API key not configured");
            return null;
        }

        try {
            // 1. GHÉP PROMPT HOÀN CHỈNH: Dùng instruction và examples để AI trả về format cố định
            String fullPrompt = systemInstruction + "\n\n" + fewshotExamples + "\n\n"
                    + "Khách hàng hỏi: " + prompt + "\n"
                    + "BOT_RESPONSE:";

            // 2. Tạo Request Body chuẩn Gemini API
            Object body = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    // Sửa prompt thành fullPrompt
                                    "parts", List.of(Map.of("text", fullPrompt))
                            )
                    ),
                    // Quan trọng: Set Temperature = 0.0 để AI phải tuân theo luật Prompt
                    "generationConfig", Map.of("temperature", 0.0)
            );

            String path = "/v1beta/models/" + model + ":" + action;
            logger.debug("Calling Gemini at path={} (action={}) with prompt length {}", path, action, fullPrompt.length());

            Mono<Map> respMono = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path(path).queryParam("key", apiKey).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(
                            (HttpStatusCode status) -> status.isError(),
                            clientResponse -> clientResponse.bodyToMono(String.class).flatMap(bodyStr -> {
                                String msg = "Gemini error " + clientResponse.statusCode() + ": " + bodyStr;
                                logger.error("Gemini returned error status={} body={}", clientResponse.statusCode(), bodyStr);
                                return Mono.error(new RuntimeException(msg));
                            })
                    )
                    .bodyToMono(Map.class)
                    .timeout(timeout)
                    .doOnError(e -> logger.error("Gemini call error (exception): {}", e.getMessage(), e));

            Map<?, ?> resp = respMono.onErrorResume(e -> Mono.empty()).block();

            if (resp == null) {
                logger.warn("No response from Gemini (null).");
                return null;
            }

            // 3. Parse response theo chuẩn Gemini API (candidates -> content -> parts -> text)
            if (resp.containsKey("candidates")) {
                Object cands = resp.get("candidates");
                if (cands instanceof List && !((List<?>) cands).isEmpty()) {
                    Object firstCand = ((List<?>) cands).get(0);
                    if (firstCand instanceof Map) {
                        Object contentObj = ((Map<?, ?>) firstCand).get("content");
                        if (contentObj instanceof Map) {
                            Object partsObj = ((Map<?, ?>) contentObj).get("parts");
                            if (partsObj instanceof List && !((List<?>) partsObj).isEmpty()) {
                                Object firstPart = ((List<?>) partsObj).get(0);
                                if (firstPart instanceof Map) {
                                    Object text = ((Map<?, ?>) firstPart).get("text");
                                    if (text != null) return text.toString().trim(); // Trả về text đã trim
                                }
                            }
                        }
                    }
                }
            }


            if (resp.containsKey("output")) return resp.get("output").toString();
            if (resp.containsKey("text")) return resp.get("text").toString();
            if (resp.containsKey("content")) return resp.get("content").toString();

            // fallback: return resp as string
            return resp.toString();

        } catch (Exception ex) {
            logger.error("Exception while calling Gemini: {}", ex.getMessage(), ex);
            return null;
        }
    }
}