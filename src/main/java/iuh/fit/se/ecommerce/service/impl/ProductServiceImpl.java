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
import iuh.fit.se.ecommerce.service.impl.SiteNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final Cloudinary cloudinary;
    private final SiteNotificationService siteNotificationService;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        mapCreateRequestToProduct(product, request);
        Product saved = productRepository.save(product);
        siteNotificationService.saveAndBroadcast(
                "product",
                "Sản phẩm mới: " + saved.getName(),
                saved.getPrice() != null ? "Giá: " + saved.getPrice() : "",
                "/product-detail.html?id=" + saved.getId(),
                saved.getId(),
                null);
        return ProductMapper.toProductResponse(saved);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Không tìm thấy sản phẩm"));

        mapUpdateRequestToProduct(product, request);
        Product saved = productRepository.save(product);
        siteNotificationService.saveAndBroadcast(
                "product",
                "Cập nhật sản phẩm: " + saved.getName(),
                saved.getPrice() != null ? "Giá: " + saved.getPrice() : "",
                "/product-detail.html?id=" + saved.getId(),
                saved.getId(),
                null);
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
        try {
            productType = ProductType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
        }
        return productRepository.findByProductType(productType).stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByTypeWithFilters(String type, String cpu, String screenSize,
                                                               String switchType, String connection, String dpi,
                                                               String resolution, String refreshRate, String usage,
                                                               String accessoryType, String size, String typeFilter) {
        ProductType productType;
        try {
            productType = ProductType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
        }
        
        List<Product> products = productRepository.findByProductType(productType);
        
        // Filter by CPU (for LAPTOP, PC)
        if (cpu != null && !cpu.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "CPU", cpu))
                    .collect(Collectors.toList());
        }
        
        // Filter by Screen Size (for LAPTOP, MONITOR)
        if (screenSize != null && !screenSize.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Screen Size", screenSize) || hasSpec(p, "Màn hình", screenSize))
                    .collect(Collectors.toList());
        }
        
        // Filter by Switch Type (for KEYBOARD)
        if (switchType != null && !switchType.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Switch Type", switchType) || hasSpec(p, "Loại switch", switchType))
                    .collect(Collectors.toList());
        }
        
        // Filter by Connection (for KEYBOARD, MOUSE, HEADPHONE)
        if (connection != null && !connection.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Connection", connection) || hasSpec(p, "Kết nối", connection))
                    .collect(Collectors.toList());
        }
        
        // Filter by DPI (for MOUSE)
        if (dpi != null && !dpi.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "DPI", dpi))
                    .collect(Collectors.toList());
        }
        
        // Filter by Resolution (for MONITOR)
        if (resolution != null && !resolution.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Resolution", resolution) || hasSpec(p, "Độ phân giải", resolution))
                    .collect(Collectors.toList());
        }
        
        // Filter by Refresh Rate (for MONITOR)
        if (refreshRate != null && !refreshRate.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Refresh Rate", refreshRate) || hasSpec(p, "Tần số quét", refreshRate))
                    .collect(Collectors.toList());
        }
        
        // Filter by Usage (for PC, HEADPHONE)
        if (usage != null && !usage.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Usage", usage) || hasSpec(p, "Nhu cầu", usage) || 
                               hasSpec(p, "Nhu cầu sử dụng", usage))
                    .collect(Collectors.toList());
        }
        
        // Filter by Accessory Type (for ACCESSORY)
        if (accessoryType != null && !accessoryType.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Type", accessoryType) || hasSpec(p, "Loại phụ kiện", accessoryType))
                    .collect(Collectors.toList());
        }
        
        // Filter by Size (for KEYBOARD, MONITOR)
        if (size != null && !size.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Size", size) || hasSpec(p, "Kích thước", size))
                    .collect(Collectors.toList());
        }
        
        // Filter by Type (for MOUSE, MONITOR, HEADPHONE)
        if (typeFilter != null && !typeFilter.isBlank()) {
            products = products.stream()
                    .filter(p -> hasSpec(p, "Type", typeFilter) || hasSpec(p, "Loại", typeFilter))
                    .collect(Collectors.toList());
        }
        
        return products.stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());
    }
    
    private boolean hasSpec(Product product, String specName, String specValue) {
        if (product.getSpecifications() == null || product.getSpecifications().isEmpty()) {
            return false;
        }
        String lowerSpecValue = specValue.toLowerCase();
        return product.getSpecifications().stream()
                .anyMatch(spec -> {
                    String specNameLower = spec.getSpecName() != null ? spec.getSpecName().toLowerCase() : "";
                    String specValueLower = spec.getSpecValue() != null ? spec.getSpecValue().toLowerCase() : "";
                    return (specNameLower.contains(specName.toLowerCase()) || 
                           specName.toLowerCase().contains(specNameLower)) &&
                           (specValueLower.contains(lowerSpecValue) || 
                            lowerSpecValue.contains(specValueLower));
                });
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
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (qLower.startsWith("spec:")) {
            String term = q.substring("spec:".length()).trim();
            if (!term.isEmpty())
                criteria.setSpecTerms(List.of(term));
        } else if (qLower.startsWith("promotion:")) {
            // handle promotion label via text fallback (productRepository.search will look
            // at promotion indirectly)
            criteria.setText(q);
        } else {
            // Natural-language parsing heuristics (brand words, types, price phrases,
            // specs)
            // brands
            String[] knownBrands = new String[] { "dell", "hp", "asus", "acer", "lenovo", "apple", "msi", "lg" };
            for (String b : knownBrands) {
                if (qLower.contains(b)) {
                    criteria.setBrand(b);
                    break;
                }
            }
            // types
            if (qLower.contains("gaming"))
                criteria.setProductType("GAMING");
            else if (qLower.contains("ultrabook"))
                criteria.setProductType("ULTRABOOK");
            else if (qLower.contains("workstation"))
                criteria.setProductType("WORKSTATION");
            else if (qLower.contains("accessory") || qLower.contains("phụ kiện") || qLower.contains("phu kien"))
                criteria.setProductType("ACCESSORY");

            // price patterns
            try {
                java.util.regex.Pattern pRange = java.util.regex.Pattern.compile(
                        "(\\d+(?:[.,]?\\d+)?)\\s*-\\s*(\\d+(?:[.,]?\\d+)?)(?:\\s*(triệu|m|vnđ|vnd))?",
                        java.util.regex.Pattern.CASE_INSENSITIVE);
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
                    java.util.regex.Pattern pUnder = java.util.regex.Pattern.compile(
                            "dưới\\s+(\\d+(?:[.,]?\\d+)?)(?:\\s*(triệu|m|vnđ|vnd))?",
                            java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher mUnder = pUnder.matcher(qLower);
                    if (mUnder.find()) {
                        String a = mUnder.group(1).replaceAll("[.,]", "");
                        String unit = mUnder.group(2);
                        BigDecimal aVal = new BigDecimal(a);
                        if (unit != null && unit.toLowerCase().contains("triệu"))
                            aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                        criteria.setMaxPrice(aVal);
                    } else {
                        java.util.regex.Pattern pOver = java.util.regex.Pattern.compile(
                                "trên\\s+(\\d+(?:[.,]?\\d+)?)(?:\\s*(triệu|m|vnđ|vnd))?",
                                java.util.regex.Pattern.CASE_INSENSITIVE);
                        java.util.regex.Matcher mOver = pOver.matcher(qLower);
                        if (mOver.find()) {
                            String a = mOver.group(1).replaceAll("[.,]", "");
                            String unit = mOver.group(2);
                            BigDecimal aVal = new BigDecimal(a);
                            if (unit != null && unit.toLowerCase().contains("triệu"))
                                aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                            criteria.setMinPrice(aVal);
                        } else {
                            java.util.regex.Pattern pSingle = java.util.regex.Pattern.compile(
                                    "(\\d+(?:[.,]?\\d+)?)\\s*(triệu|m|vnđ|vnd)",
                                    java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher mSingle = pSingle.matcher(qLower);
                            if (mSingle.find()) {
                                String a = mSingle.group(1).replaceAll("[.,]", "");
                                String unit = mSingle.group(2);
                                BigDecimal aVal = new BigDecimal(a);
                                if (unit != null && unit.toLowerCase().contains("triệu"))
                                    aVal = aVal.multiply(BigDecimal.valueOf(1_000_000L));
                                criteria.setMinPrice(
                                        aVal.multiply(BigDecimal.valueOf(8)).divide(BigDecimal.valueOf(10)));
                                criteria.setMaxPrice(
                                        aVal.multiply(BigDecimal.valueOf(12)).divide(BigDecimal.valueOf(10)));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // ignore parse exceptions
            }

            // spec terms
            List<String> specTerms = new ArrayList<>();
            java.util.regex.Pattern pSpecGb = java.util.regex.Pattern.compile("(\\d+)\\s*gb",
                    java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher mSpecGb = pSpecGb.matcher(qLower);
            while (mSpecGb.find()) {
                specTerms.add(mSpecGb.group(0));
            }
            if (qLower.contains("ram"))
                specTerms.add("ram");
            if (qLower.contains("ssd"))
                specTerms.add("ssd");
            if (qLower.contains("hdd"))
                specTerms.add("hdd");
            if (qLower.contains("cpu") || qLower.contains("core") || qLower.contains("intel") || qLower.contains("amd"))
                specTerms.add("cpu");
            if (qLower.contains("vga") || qLower.contains("card"))
                specTerms.add("vga");

            // --- THÊM CÁC THUẬT NGỮ VỀ MÀN HÌNH ---
            if (qLower.contains("màn hình") || qLower.contains("man hinh") || qLower.contains("display"))
                specTerms.add("màn hình");
            if (qLower.contains("120hz") || qLower.contains("144hz") || qLower.contains("165hz")
                    || qLower.contains("240hz"))
                specTerms.add("tần số quét cao");
            if (qLower.contains("oled") || qLower.contains("ips") || qLower.contains("amoled"))
                specTerms.add("công nghệ màn hình");
            // ----------------------------------------

            if (!specTerms.isEmpty())
                criteria.setSpecTerms(specTerms);

            // if no structured filters detected, set text for free-text search
            if (criteria.getBrand() == null && criteria.getProductType() == null && criteria.getMinPrice() == null
                    && criteria.getMaxPrice() == null
                    && (criteria.getSpecTerms() == null || criteria.getSpecTerms().isEmpty())) {
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

    @Override
    public Map<String, Object> searchAutocomplete(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("products", List.of());
            result.put("totalCount", 0L);
            return result;
        }

        ProductSearchCriteria criteria = buildSearchCriteria(query);
        Pageable pageable = PageRequest.of(0, limit);
        Page<Product> page = productRepository.search(criteria, pageable);

        List<ProductResponse> products = page.getContent().stream()
                .map(ProductMapper::toProductResponse)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("totalCount", page.getTotalElements());
        return result;
    }

    @Override
    public Page<ProductResponse> searchProducts(String query, int page, int size, String sort) {
        if (query == null || query.trim().isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        ProductSearchCriteria criteria = buildSearchCriteria(query);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.search(criteria, pageable, sort);

        return productPage.map(ProductMapper::toProductResponse);
    }

    private ProductSearchCriteria buildSearchCriteria(String query) {
        String q = query.trim();
        String qLower = q.toLowerCase(Locale.ROOT);

        ProductSearchCriteria criteria = new ProductSearchCriteria();
        criteria.setText(null);

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
            criteria.setText(q);
        } else {
            // Natural-language parsing heuristics
            String[] knownBrands = new String[]{"dell","hp","asus","acer","lenovo","apple","msi","lg"};
            for (String b : knownBrands) {
                if (qLower.contains(b)) {
                    criteria.setBrand(b);
                    break;
                }
            }
            
            if (qLower.contains("gaming")) criteria.setProductType("GAMING");
            else if (qLower.contains("ultrabook")) criteria.setProductType("ULTRABOOK");
            else if (qLower.contains("workstation")) criteria.setProductType("WORKSTATION");
            else if (qLower.contains("accessory") || qLower.contains("phụ kiện") || qLower.contains("phu kien")) 
                criteria.setProductType("ACCESSORY");

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
            if (qLower.contains("màn hình") || qLower.contains("man hinh") || qLower.contains("display")) specTerms.add("màn hình");
            if (qLower.contains("120hz") || qLower.contains("144hz") || qLower.contains("165hz") || qLower.contains("240hz")) specTerms.add("tần số quét cao");
            if (qLower.contains("oled") || qLower.contains("ips") || qLower.contains("amoled")) specTerms.add("công nghệ màn hình");

            if (!specTerms.isEmpty()) criteria.setSpecTerms(specTerms);

            // if no structured filters detected, set text for free-text search
            if (criteria.getBrand() == null && criteria.getProductType() == null && criteria.getMinPrice() == null && criteria.getMaxPrice() == null && (criteria.getSpecTerms() == null || criteria.getSpecTerms().isEmpty())) {
                criteria.setText(q);
            }
        }

        return criteria;
    }

    public void mapCreateRequestToProduct(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        try {
            product.setProductType(ProductType.valueOf(request.getProductType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
        }

        if (request.getMainImage() != null) {
            product.setMainImage(uploadFile(request.getMainImage()));
        }
        if (request.getImages() != null && !request.getImages().isEmpty()) {
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

    public void mapUpdateRequestToProduct(Product product, ProductRequest request) {
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getBrand() != null) {
            product.setBrand(request.getBrand());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getProductType() != null) {
            try {
                product.setProductType(ProductType.valueOf(request.getProductType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Loai sản phẩm không hợp lệ");
            }
        }
        
        // Ảnh chính: replace nếu có upload mới
        if (request.getMainImage() != null) {
            product.setMainImage(uploadFile(request.getMainImage()));
        }
        
        // Ảnh khác: xóa các ảnh được đánh dấu xóa, sau đó thêm ảnh mới vào danh sách hiện có
        List<String> currentImages = product.getImages() != null 
            ? new ArrayList<>(product.getImages()) 
            : new ArrayList<>();
        
        // Xóa các ảnh trong danh sách imagesToDelete (chỉ xóa trong DB, không xóa trên Cloudinary)
        if (request.getImagesToDelete() != null && !request.getImagesToDelete().isEmpty()) {
            currentImages.removeAll(request.getImagesToDelete());
        }
        
        // Thêm ảnh mới vào danh sách hiện có (không replace toàn bộ)
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> newImageUrls = request.getImages().stream()
                    .map(this::uploadFile)
                    .collect(Collectors.toList());
            currentImages.addAll(newImageUrls);
        }
        
        product.setImages(currentImages);

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
}