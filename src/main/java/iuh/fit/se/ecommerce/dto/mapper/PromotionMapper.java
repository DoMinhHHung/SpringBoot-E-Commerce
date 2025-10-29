package iuh.fit.se.ecommerce.dto.mapper;

import iuh.fit.se.ecommerce.dto.request.PromotionRequest;
import iuh.fit.se.ecommerce.dto.response.PromotionResponse;
import iuh.fit.se.ecommerce.entity.Promotion;

public interface PromotionMapper {

    public static PromotionResponse toResponse(Promotion promotion) {
        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .discountPercent(promotion.getDiscountPercent())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .build();
    }

    public static Promotion fromRequest(PromotionRequest request) {
        return Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountPercent(request.getDiscountPercent())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }
}