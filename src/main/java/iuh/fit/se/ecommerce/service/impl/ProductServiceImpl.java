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
import iuh.fit.se.ecommerce.repository.PromotionRepository;
import iuh.fit.se.ecommerce.repository.SpecificationRepository;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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
}
