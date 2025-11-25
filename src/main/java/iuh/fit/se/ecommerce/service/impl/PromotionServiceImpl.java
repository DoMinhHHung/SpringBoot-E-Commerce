package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.mapper.ProductMapper;
import iuh.fit.se.ecommerce.dto.mapper.PromotionMapper;
import iuh.fit.se.ecommerce.dto.request.PromotionRequest;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.dto.response.PromotionResponse;
import iuh.fit.se.ecommerce.entity.Promotion;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.ProductRepository;
import iuh.fit.se.ecommerce.repository.PromotionRepository;
import iuh.fit.se.ecommerce.service.impl.SiteNotificationService;
import iuh.fit.se.ecommerce.service.interfaces.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final SiteNotificationService siteNotificationService;

    @Override
    public PromotionResponse createPromotion(PromotionRequest request) {
        Promotion promotion = PromotionMapper.fromRequest(request);
        Promotion saved = promotionRepository.save(promotion);
        siteNotificationService.saveAndBroadcast(
                "promotion",
                "Khuyến mãi mới: " + saved.getName(),
                saved.getDescription() == null ? "" : saved.getDescription(),
                "/promotions.html",
                null,
                null);
        return PromotionMapper.toResponse(saved);
    }

    @Override
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã khuyễn mãi"));

        if (request.getName() != null) {
            promotion.setName(request.getName());
        }

        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }

        if (request.getDiscountPercent() != null) {
            promotion.setDiscountPercent(request.getDiscountPercent());
        }

        if (request.getStartDate() != null) {
            promotion.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            promotion.setEndDate(request.getEndDate());
        }

        Promotion saved = promotionRepository.save(promotion);
        siteNotificationService.saveAndBroadcast(
                "promotion",
                "Cập nhật khuyến mãi: " + saved.getName(),
                saved.getDescription() == null ? "" : saved.getDescription(),
                "/promotions.html",
                null,
                null);
        return PromotionMapper.toResponse(saved);
    }

    @Override
    public void deletePromotion(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã khuyễn mãi"));
        promotionRepository.delete(promotion);
    }

    @Override
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã khuyễn mãi"));
        return PromotionMapper.toResponse(promotion);
    }

    @Override
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(PromotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getActivePromotions() {
        LocalDate now = LocalDate.now();
        return promotionRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(now, now).stream()
                .map(PromotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getExpiredPromotions() {
        LocalDate now = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> p.getEndDate() != null && p.getEndDate().isBefore(now))
                .map(PromotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PromotionResponse> getUpcomingPromotions() {
        LocalDate now = LocalDate.now();
        return promotionRepository.findAll().stream()
                .filter(p -> p.getStartDate() != null && p.getStartDate().isAfter(now))
                .map(PromotionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByActivePromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã khuyến mãi"));

        LocalDate now = LocalDate.now();
        // If either date is null or outside range, promotion is not active
        if (promotion.getStartDate() == null || promotion.getEndDate() == null
                || promotion.getStartDate().isAfter(now) || promotion.getEndDate().isBefore(now)) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Mã khuyến mãi chưa được kích hoạt");
        }

        return productRepository.findByPromotion(promotion).stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void assignProductsToPromotion(Long promotionId, List<Long> productIds) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã khuyến mãi"));
        if (productIds == null || productIds.isEmpty())
            return;
        List<Long> ids = productIds.stream().distinct().collect(Collectors.toList());
        List<iuh.fit.se.ecommerce.entity.Product> products = productRepository.findAllById(ids);
        products.forEach(p -> p.setPromotion(promotion));
        productRepository.saveAll(products);
    }

    @Override
    public void assignAllProductsToPromotion(Long promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy mã khuyến mãi"));
        List<iuh.fit.se.ecommerce.entity.Product> all = productRepository.findAll();
        all.forEach(p -> p.setPromotion(promotion));
        productRepository.saveAll(all);
    }

}
