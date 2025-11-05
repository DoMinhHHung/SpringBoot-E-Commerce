package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.dto.request.ProductSearchCriteria;
import iuh.fit.se.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<Product> search(ProductSearchCriteria criteria, Pageable pageable);
}

