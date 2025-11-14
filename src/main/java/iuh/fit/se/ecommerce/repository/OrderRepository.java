package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(Long orderCode);
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, iuh.fit.se.ecommerce.entity.enums.OrderStatus status);
}

