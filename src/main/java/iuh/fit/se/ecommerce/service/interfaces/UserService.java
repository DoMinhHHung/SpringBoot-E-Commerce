package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.ChangePasswordRequest;
import iuh.fit.se.ecommerce.dto.request.UpdateProfileRequest;
import iuh.fit.se.ecommerce.dto.response.PermissionResponse;
import iuh.fit.se.ecommerce.dto.response.RolePermissionResponse;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.entity.enums.Role;

import java.util.List;
import java.util.Set;

public interface UserService {
    void changePassword(String email, ChangePasswordRequest req);
    UserResponse updateProfile(String email, UpdateProfileRequest req);
    UserResponse getByEmail(String email);
    
    // Permission management methods
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(Role role);
    List<UserResponse> getUsersByPermission(String permissionCode);
    List<RolePermissionResponse> getUserRoles(Long userId);
    Set<PermissionResponse> getUserPermissions(Long userId);
    void assignRoleToUser(Long userId, Role role);

    // New: ban/unban
    void banUser(Long userId, Long adminId, String reason);
    void unbanUser(Long userId, Long adminId);
}
