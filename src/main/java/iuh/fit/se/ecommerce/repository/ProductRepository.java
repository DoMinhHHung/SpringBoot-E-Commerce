package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Product;
import iuh.fit.se.ecommerce.entity.Promotion;
import iuh.fit.se.ecommerce.entity.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
    List<Product> findByProductType(ProductType productType);
    List<Product> findByPromotion(Promotion promotion);
    
    @Query("SELECT p FROM Product p WHERE p.promotion IS NOT NULL " +
           "AND p.promotion.startDate <= CURRENT_DATE " +
           "AND p.promotion.endDate >= CURRENT_DATE " +
           "ORDER BY p.promotion.discountPercent DESC")
    List<Product> findHotSaleProducts();
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    /**
     * Atomic update stock - chỉ update khi stock >= quantity
     * @return Số dòng bị ảnh hưởng (0 nếu stock không đủ, 1 nếu thành công)
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}