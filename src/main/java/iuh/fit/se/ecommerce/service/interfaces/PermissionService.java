package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.response.PermissionResponse;
import iuh.fit.se.ecommerce.dto.response.RolePermissionResponse;
import iuh.fit.se.ecommerce.entity.Permission;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.Role;

import java.util.List;
import java.util.Set;

public interface PermissionService {
    // Permission checks
    boolean hasPermission(User user, String permissionCode);
    boolean hasAnyPermission(User user, String... permissionCodes);
    boolean hasAllPermissions(User user, String... permissionCodes);
    
    // Get permissions
    Set<Permission> getPermissionsByRole(Role role);
    Set<String> getPermissionCodesByRole(Role role);
    
    // Permission management
    void assignPermissionToRole(Role role, String permissionCode);
    void removePermissionFromRole(Role role, String permissionCode);
    
    // Get all permissions
    List<PermissionResponse> getAllPermissions();
    PermissionResponse getPermissionByCode(String code);
    
    // Role-Permission management
    List<RolePermissionResponse> getAllRolesWithPermissions();
    RolePermissionResponse getRoleWithPermissions(Role role);
    List<RolePermissionResponse> getRolesByPermission(String permissionCode);
}

