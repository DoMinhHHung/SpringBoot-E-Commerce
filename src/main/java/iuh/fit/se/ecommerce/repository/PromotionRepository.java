package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByStartDateBeforeAndEndDateAfter(LocalDate now1, LocalDate now2);
}