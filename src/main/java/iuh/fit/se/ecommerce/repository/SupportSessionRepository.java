package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.SupportSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportSessionRepository extends JpaRepository<SupportSession, String> {
    List<SupportSession> findByStatus(String status);
}

