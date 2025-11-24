package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.PromotionRequest;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.dto.response.PromotionResponse;

import java.util.List;

public interface PromotionService {
    PromotionResponse createPromotion(PromotionRequest request);
    PromotionResponse updatePromotion(Long id, PromotionRequest request);
    void deletePromotion(Long id);
    PromotionResponse getPromotionById(Long id);
    List<PromotionResponse> getAllPromotions();
    List<PromotionResponse> getActivePromotions();

    List<PromotionResponse> getExpiredPromotions();
    List<PromotionResponse> getUpcomingPromotions();
    List<ProductResponse> getProductsByActivePromotion(Long promotionId);

    // new methods
    void assignProductsToPromotion(Long promotionId, List<Long> productIds);
    void assignAllProductsToPromotion(Long promotionId);
}
