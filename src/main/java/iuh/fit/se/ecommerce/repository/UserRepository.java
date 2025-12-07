package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<User> findByRole(Role role);

    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.createdAt >= :start
              AND u.createdAt < :end
            """)
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
