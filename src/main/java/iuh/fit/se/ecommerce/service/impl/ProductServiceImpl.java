package iuh.fit.se.ecommerce.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import iuh.fit.se.ecommerce.dto.mapper.ProductMapper;
import iuh.fit.se.ecommerce.dto.request.ProductRequest;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.entity.Product;
import iuh.fit.se.ecommerce.entity.Promotion;
import iuh.fit.se.ecommerce.entity.Specification;
import iuh.fit.se.ecommerce.entity.enums.ProductType;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.ProductRepository;
import iuh.fit.se.ecommerce.dto.request.ProductSearchCriteria;
import iuh.fit.se.ecommerce.repository.PromotionRepository;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final Cloudinary cloudinary;


    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapCreateRequestToProduct(product, request);
        Product saved = productRepository.save(product);
        return ProductMapper.toProductResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy sản phẩm"));

        mapUpdateRequestToProduct(product, request);
        Product saved = productRepository.save(product);
        return ProductMapper.toProductResponse(saved);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy sản phẩm"));
        productRepository.delete(product);
    }

    @Override
    public ProductDetailResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy sản phẩm"));
        return ProductMapper.toProductDetailResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByType(String type) {
        ProductType productType;
        try{
            productType = ProductType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
        }
        return productRepository.findByProductType(productType).stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getHotSaleProducts(int limit) {
        List<Product> hotSaleProducts = productRepository.findHotSaleProducts();
        return hotSaleProducts.stream()
                .limit(limit > 0 ? limit : Integer.MAX_VALUE)
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }


    @Override
    public List<ProductResponse> findByQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String q = query.trim();
        String qLower = q.toLowerCase(Locale.ROOT);

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setText(null); // only set if no structured filters detected

        // detect structured prefixes first
        if (qLower.startsWith("brand:")) {
            criteria.setBrand(q.substring("brand:".length()).trim());
        } else if (qLower.startsWith("type:")) {
            criteria.setProductType(q.substring("type:".length()).trim());
        } else if (qLower.startsWith("price:")) {
            String range = q.substring("price:".length()).trim();
            if (range.contains("-")) {
                String[] parts = range.split("-", 2);
                try {
                    criteria.setMinPrice(new BigDecimal(parts[0].trim()));
                    criteria.setMaxPrice(new BigDecimal(parts[1].trim()));
                } catch (NumberFormatException ignored) {}
            }
        } else if (qLower.startsWith("spec:")) {
            String term = q.substring("spec:".length()).trim();
            if (!term.isEmpty()) criteria.setSpecTerms(List.of(term));
        } else if (qLower.startsWith("promotion:")) {
            // handle promotion label via text fallback (productRepository.search will look at promotion indirectly)
            criteria.setText(q);
        } else {
            // Natural-language parsing heuristics (brand words, types, price phrases, specs)
            // brands
            String[] knownBrands = new String[]{"dell","hp","asus","acer","lenovo","apple","msi","lg"};
            for (String b : knownBrands) {
                if (qLower.contains(b)) {
                    criteria.setBrand(b);
                    break;
                }
            }
            // types
            if (qLower.contains("gaming")) criteria.setProductType("GAMING");
            else if (qLower.contains("ultrabook")) criteria.setProductType("ULTRABOOK");
            else if (qLower.contains("workstation")) criteria.setProductType("WORKSTATION");
            else if (qLower.contains("accessory") || qLower.contains("phụ kiện") || qLower.contains("phu kien")) criteria.setProductType("ACCESSORY");

            // price patterns
            try {
                java.util.regex.Pattern pRange = java.util.regex.Pattern.compile("(\\d+(?:[.,]?\\d+)?)\\s*-\\s*(\\d+(?:[.,]?\\d+)?)(?:\\s*(triệu|m|vnđ|vnd))?", java.util.regex.Pattern.CASE_INSENSITIVE);
                java.util.regex.Matcher mRange = pRange.matcher(qLower);
                if (mRange.find()) {
                    String a = mRange.group(1).replaceAll("[.,]", "");
                    String b = mRange.group(2).replaceAll("[.,]", "");
                    String unit = mRange.group(3);
                    BigDecimal aVal = new BigDecimal(a);
                    BigDecimal bVal = new BigDecimal(b);
                    if (unit != null && unit.toLowerCase().contains("triệu")) {
                        aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                        bVal = bVal.multiply(BigDecimal.valueOf(1_000_000L));
                    }
                    criteria.setMinPrice(aVal);
                    criteria.setMaxPrice(bVal);
                } else {
                    java.util.regex.Pattern pUnder = java.util.regex.Pattern.compile("dưới\\s+(\\d+(?:[.,]?\\d+)?)(?:\\s*(triệu|m|vnđ|vnd))?", java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher mUnder = pUnder.matcher(qLower);
                    if (mUnder.find()) {
                        String a = mUnder.group(1).replaceAll("[.,]", "");
                        String unit = mUnder.group(2);
                        BigDecimal aVal = new BigDecimal(a);
                        if (unit != null && unit.toLowerCase().contains("triệu")) aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                        criteria.setMaxPrice(aVal);
                    } else {
                        java.util.regex.Pattern pOver = java.util.regex.Pattern.compile("trên\\s+(\\d+(?:[.,]?\\d+)?)(?:\\s*(triệu|m|vnđ|vnd))?", java.util.regex.Pattern.CASE_INSENSITIVE);
                        java.util.regex.Matcher mOver = pOver.matcher(qLower);
                        if (mOver.find()) {
                            String a = mOver.group(1).replaceAll("[.,]", "");
                            String unit = mOver.group(2);
                            BigDecimal aVal = new BigDecimal(a);
                            if (unit != null && unit.toLowerCase().contains("triệu")) aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                            criteria.setMinPrice(aVal);
                        } else {
                            java.util.regex.Pattern pSingle = java.util.regex.Pattern.compile("(\\d+(?:[.,]?\\d+)?)\\s*(triệu|m|vnđ|vnd)", java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher mSingle = pSingle.matcher(qLower);
                            if (mSingle.find()) {
                                String a = mSingle.group(1).replaceAll("[.,]", "");
                                String unit = mSingle.group(2);
                                BigDecimal aVal = new BigDecimal(a);
                                if (unit != null && unit.toLowerCase().contains("triệu")) aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                                criteria.setMinPrice(aVal.multiply(BigDecimal.valueOf(8)).divide(BigDecimal.valueOf(10)));
                                criteria.setMaxPrice(aVal.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(10)));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // ignore parse exceptions
            }

            // spec terms
            List<String> specTerms = new ArrayList<>();
            java.util.regex.Pattern pSpecGb = java.util.regex.Pattern.compile("(\\d+)\\s*gb", java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher mSpecGb = pSpecGb.matcher(qLower);
            while (mSpecGb.find()) {
                specTerms.add(mSpecGb.group(0));
            }
            if (qLower.contains("ram")) specTerms.add("ram");
            if (qLower.contains("ssd")) specTerms.add("ssd");
            if (qLower.contains("hdd")) specTerms.add("hdd");
            if (qLower.contains("cpu") || qLower.contains("core") || qLower.contains("intel") || qLower.contains("amd")) specTerms.add("cpu");
            if (qLower.contains("vga") || qLower.contains("card")) specTerms.add("vga");

            // --- THÊM CÁC THUẬT NGỮ VỀ MÀN HÌNH ---
            if (qLower.contains("màn hình") || qLower.contains("man hinh") || qLower.contains("display")) specTerms.add("màn hình");
            if (qLower.contains("120hz") || qLower.contains("144hz") || qLower.contains("165hz") || qLower.contains("240hz")) specTerms.add("tần số quét cao");
            if (qLower.contains("oled") || qLower.contains("ips") || qLower.contains("amoled")) specTerms.add("công nghệ màn hình");
            // ----------------------------------------

            if (!specTerms.isEmpty()) criteria.setSpecTerms(specTerms);

            // if no structured filters detected, set text for free-text search
            if (criteria.getBrand() == null && criteria.getProductType() == null && criteria.getMinPrice() == null && criteria.getMaxPrice() == null && (criteria.getSpecTerms() == null || criteria.getSpecTerms().isEmpty())) {
                criteria.setText(q);
            }
        }

        // call repository with paging
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = productRepository.search(criteria, pageable);

        return page.getContent().stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    public void mapCreateRequestToProduct(Product product, ProductRequest request){
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        try {
            product.setProductType(ProductType.valueOf(request.getProductType().toUpperCase()));
        }catch (IllegalArgumentException e){
            throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
        }

        if(request.getMainImage() != null){
            product.setMainImage(uploadFile(request.getMainImage()));
        }
        if(request.getImages() != null && !request.getImages().isEmpty()){
            List<String> urls = request.getImages().stream()
                    .map(this::uploadFile)
                    .collect(Collectors.toList());
            product.setImages(urls);
        }

        if (request.getPromotionId() != null) {
            Promotion promo = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Promotion not found"));
            product.setPromotion(promo);
        }

        if (request.getSpecifications() != null) {
            List<Specification> specs = request.getSpecifications().stream().map(specReq -> {
                Specification spec = new Specification();
                spec.setSpecName(specReq.getSpecName());
                spec.setSpecValue(specReq.getSpecValue());
                spec.setProduct(product);
                return spec;
            }).collect(Collectors.toList());
            product.setSpecifications(specs);
        }
    }

    public void mapUpdateRequestToProduct(Product product, ProductRequest request){
        if(request.getName() != null){
            product.setName(request.getName());
        }
        if(request.getBrand() != null){
            product.setBrand(request.getBrand());
        }
        if(request.getDescription() != null){
            product.setDescription(request.getDescription());
        }
        if(request.getPrice() != null){
            product.setPrice(request.getPrice());
        }
        if(request.getStock() != null){
            product.setStock(request.getStock());
        }
        if(request.getProductType() != null){
            try {
                product.setProductType(ProductType.valueOf(request.getProductType().toUpperCase()));
            }catch (IllegalArgumentException e){
                throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
            }
        }
        if(request.getMainImage() != null){
            product.setMainImage(uploadFile(request.getMainImage()));
        }
        if(request.getImages() != null && !request.getImages().isEmpty()){
            List<String> urls = request.getImages().stream()
                    .map(this::uploadFile)
                    .collect(Collectors.toList());
            product.setImages(urls);
        }

        if (request.getPromotionId() != null) {
            Promotion promo = promotionRepository.findById(request.getPromotionId())
                    .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Promotion not found"));
            product.setPromotion(promo);
        }

        if (request.getSpecifications() != null) {
            List<Specification> specs = request.getSpecifications().stream().map(specReq -> {
                Specification spec = new Specification();
                spec.setSpecName(specReq.getSpecName());
                spec.setSpecValue(specReq.getSpecValue());
                spec.setProduct(product);
                return spec;
            }).collect(Collectors.toList());
            product.setSpecifications(specs);
        }
    }

    private String uploadFile(MultipartFile file) {
        try {
            var result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Upload image failed");
        }
    }
    @Override
    public List<ProductResponse> getProductsByType(ProductType type) {
        List<Product> products = productRepository.findByProductType(type);
        return products.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    // Mapping từ Product -> ProductResponse
    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .brand(p.getBrand())
                .price(p.getPrice())
                .mainImage(p.getMainImage())
                .build();
    }
}