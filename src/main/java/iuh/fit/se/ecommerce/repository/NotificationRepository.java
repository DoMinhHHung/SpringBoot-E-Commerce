package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findTop50ByOrderByTimestampDesc();
}

