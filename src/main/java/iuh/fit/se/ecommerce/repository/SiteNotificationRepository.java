package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteNotificationRepository extends JpaRepository<NotificationEntity, Long> {
}

