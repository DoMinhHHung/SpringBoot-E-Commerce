package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.ProductRequest;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    ProductDetailResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getProductsByType(String type);
    List<ProductResponse> findByQuery(String query);
    List<ProductResponse> getHotSaleProducts(int limit);
    Map<String, Object> searchAutocomplete(String query, int limit);
    Page<ProductResponse> searchProducts(String query, int page, int size, String sort);
}
