package iuh.fit.se.ecommerce.dto.mapper;

import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ProductMapper {

    static ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .price(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO)
                .priceAfterDiscount(calculatePriceAfterDiscount(product))
                .mainImage(product.getMainImage())
                .build();
    }

    static ProductDetailResponse toProductDetailResponse(Product product) {
        List<Map<String, String>> specs = product.getSpecifications() != null
                ? product.getSpecifications().stream()
                .map(s -> Map.of(s.getSpecName(), s.getSpecValue()))
                .collect(Collectors.toList())
                : Collections.emptyList();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .stock(product.getStock() != null ? product.getStock() : 0)
                .price(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO)
                .priceAfterDiscount(calculatePriceAfterDiscount(product))
                .description(product.getDescription())
                .mainImage(product.getMainImage())
                .images(product.getImages() != null ? product.getImages() : Collections.emptyList())
                .productType(product.getProductType() != null ? product.getProductType().name() : null)
                .specifications(specs)
                .promotionId(product.getPromotion() != null ? product.getPromotion().getId() : null)
                .build();
    }

    private static BigDecimal calculatePriceAfterDiscount(Product product) {
        if (product.getPrice() == null) return BigDecimal.ZERO;

        var promotion = product.getPromotion();
        if (promotion != null
                && promotion.getStartDate() != null
                && promotion.getEndDate() != null) {

            LocalDate now = LocalDate.now();
            LocalDate start = promotion.getStartDate();
            LocalDate end = promotion.getEndDate();

            boolean active = (now.isEqual(start) || now.isAfter(start)) && (now.isEqual(end) || now.isBefore(end));
            if (active) {
                BigDecimal discountPercent = BigDecimal.valueOf(promotion.getDiscountPercent());
                BigDecimal discountMultiplier = BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal.valueOf(100)));
                return product.getPrice().multiply(discountMultiplier);
            }
        }
        return product.getPrice();
    }
}
