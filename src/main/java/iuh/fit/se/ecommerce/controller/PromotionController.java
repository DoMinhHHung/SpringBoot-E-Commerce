package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.PromotionRequest;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.dto.response.PromotionResponse;
import iuh.fit.se.ecommerce.service.interfaces.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.createPromotion(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable Long id,
                                                             @RequestBody PromotionRequest request) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(Map.of("message", "Promotion deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }
    @GetMapping("/expired")
    public ResponseEntity<List<PromotionResponse>> getExpiredPromotions() {
        return ResponseEntity.ok(promotionService.getExpiredPromotions());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<PromotionResponse>> getUpcomingPromotions() {
        return ResponseEntity.ok(promotionService.getUpcomingPromotions());
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByActivePromotion(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getProductsByActivePromotion(id));
    }

    // Assign selected products to promotion
    @PostMapping("/{id}/products")
    public ResponseEntity<?> assignProducts(@PathVariable Long id, @RequestBody List<Long> productIds) {
        promotionService.assignProductsToPromotion(id, productIds);
        return ResponseEntity.ok(Map.of("message", "Assigned products to promotion"));
    }

    // Assign all products to promotion
    @PostMapping("/{id}/products/assign-all")
    public ResponseEntity<?> assignAllProducts(@PathVariable Long id) {
        promotionService.assignAllProductsToPromotion(id);
        return ResponseEntity.ok(Map.of("message", "Assigned all products to promotion"));
    }

}
