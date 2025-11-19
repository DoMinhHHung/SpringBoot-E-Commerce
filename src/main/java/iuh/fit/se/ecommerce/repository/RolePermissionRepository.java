package iuh.fit.se.ecommerce.repository;

import iuh.fit.se.ecommerce.entity.Permission;
import iuh.fit.se.ecommerce.entity.RolePermission;
import iuh.fit.se.ecommerce.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermission.RolePermissionId> {
    Set<RolePermission> findByRole(Role role);
    boolean existsByRoleAndPermission(Role role, Permission permission);
    void deleteByRole(Role role);
    
    @Query("SELECT rp.role FROM RolePermission rp WHERE rp.permission.code = :permissionCode")
    Set<Role> findRolesByPermissionCode(@Param("permissionCode") String permissionCode);
}

