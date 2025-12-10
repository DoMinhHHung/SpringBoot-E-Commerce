package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ProductRequest;
import iuh.fit.se.ecommerce.dto.response.ProductDetailResponse;
import iuh.fit.se.ecommerce.dto.response.ProductResponse;
import iuh.fit.se.ecommerce.entity.Product;
import iuh.fit.se.ecommerce.entity.enums.ProductType;
import iuh.fit.se.ecommerce.repository.ProductRepository;
import iuh.fit.se.ecommerce.repository.SpecificationRepository;
import iuh.fit.se.ecommerce.service.interfaces.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    private final SpecificationRepository specificationRepository;
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@ModelAttribute ProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @ModelAttribute ProductRequest request,
                                                         @RequestParam(required = false) String imagesToDelete) {
        // Parse imagesToDelete từ JSON string trong FormData
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<String> imagesToDeleteList = mapper.readValue(imagesToDelete, 
                    new TypeReference<List<String>>() {});
                request.setImagesToDelete(imagesToDeleteList);
            } catch (Exception e) {
                // Log error nhưng không throw để không block update
                System.err.println("Error parsing imagesToDelete: " + e.getMessage());
            }
        }
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
    public ResponseEntity<List<ProductResponse>> getProductsByType(
            @PathVariable String type,
            @RequestParam(required = false) String cpu,
            @RequestParam(required = false) String screenSize,
            @RequestParam(required = false) String switchType,
            @RequestParam(required = false) String connection,
            @RequestParam(required = false) String dpi,
            @RequestParam(required = false) String resolution,
            @RequestParam(required = false) String refreshRate,
            @RequestParam(required = false) String usage,
            @RequestParam(required = false) String accessoryType,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String typeFilter) {
        // If any filter is provided, use the new method with filters
        if (cpu != null || screenSize != null || switchType != null || connection != null ||
            dpi != null || resolution != null || refreshRate != null || usage != null ||
            accessoryType != null || size != null || typeFilter != null) {
            return ResponseEntity.ok(productService.getProductsByTypeWithFilters(
                    type, cpu, screenSize, switchType, connection, dpi, resolution,
                    refreshRate, usage, accessoryType, size, typeFilter));
        }
        // Otherwise, use the original method
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

    //api filter theo spec
    @GetMapping("/components/accessory/{specName}")
    public List<Product> getAccessoryComponents(@PathVariable String specName) {
        return productRepository.findComponentsBySpecAndType(ProductType.ACCESSORY, specName);
    }

    @DeleteMapping("/specifications/{specId}")
    public ResponseEntity<Map<String, String>> deleteSpecification(@PathVariable Long specId) {
        specificationRepository.deleteById(specId);
        return ResponseEntity.ok(Map.of("message", "Specification deleted successfully"));
    }
}
