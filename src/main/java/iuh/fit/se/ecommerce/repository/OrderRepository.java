package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(Long orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);
    
    // Pagination methods
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status, Pageable pageable);
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Search methods
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN o.items oi " +
           "LEFT JOIN oi.product p " +
           "WHERE o.user = :user " +
           "AND (CONCAT('', o.orderCode) LIKE CONCAT('%', :search, '%') OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> searchUserOrders(@Param("user") User user, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN o.items oi " +
           "LEFT JOIN oi.product p " +
           "WHERE o.user = :user " +
           "AND o.status = :status " +
           "AND (CONCAT('', o.orderCode) LIKE CONCAT('%', :search, '%') OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY o.createdAt DESC")
    Page<Order> searchUserOrdersByStatus(@Param("user") User user, @Param("status") OrderStatus status, 
                                          @Param("search") String search, Pageable pageable);
}

