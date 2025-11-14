package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Order;
import iuh.fit.se.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder(Order order);
    Optional<Payment> findByPaymentLinkId(String paymentLinkId);
}

