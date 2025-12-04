package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ProductRequest;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@ModelAttribute ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @ModelAttribute ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ProductResponse>> getProductsByType(@PathVariable String type) {
        return ResponseEntity.ok(productService.getProductsByType(type));
    }

    @GetMapping("/hot-sale")
    public ResponseEntity<List<ProductResponse>> getHotSaleProducts(
            @RequestParam(required = false, defaultValue = "6") int limit) {
        return ResponseEntity.ok(productService.getHotSaleProducts(limit));
    }

    @GetMapping("/search/autocomplete")
    public ResponseEntity<Map<String, Object>> searchAutocomplete(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(productService.searchAutocomplete(q, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "default") String sort) {
        return ResponseEntity.ok(productService.searchProducts(q, page, size, sort));
    }
}
