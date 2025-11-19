package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.mapper.PermissionMapper;
import iuh.fit.se.ecommerce.dto.request.AssignPermissionRequest;
import iuh.fit.se.ecommerce.dto.request.AssignRoleRequest;
import iuh.fit.se.ecommerce.dto.response.PermissionResponse;
import iuh.fit.se.ecommerce.dto.response.RolePermissionResponse;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.service.interfaces.PermissionService;
import iuh.fit.se.ecommerce.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionManagementController {

    private final PermissionService permissionService;
    private final UserService userService;
    private final PermissionMapper permissionMapper;

    // ========== PERMISSIONS ==========
    
    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{permissionCode}")
    public ResponseEntity<PermissionResponse> getPermissionByCode(@PathVariable String permissionCode) {
        return ResponseEntity.ok(permissionService.getPermissionByCode(permissionCode));
    }

    // ========== ROLES ==========
    
    @GetMapping("/roles")
    public ResponseEntity<List<RolePermissionResponse>> getAllRoles() {
        return ResponseEntity.ok(permissionService.getAllRolesWithPermissions());
    }

    @GetMapping("/roles/{roleName}")
    public ResponseEntity<RolePermissionResponse> getRoleWithPermissions(@PathVariable String roleName) {
        Role role = Role.valueOf(roleName.toUpperCase());
        return ResponseEntity.ok(permissionService.getRoleWithPermissions(role));
    }

    // ========== ROLE - PERMISSION MANAGEMENT ==========
    
    @GetMapping("/roles/{roleName}/permissions")
    public ResponseEntity<Set<PermissionResponse>> getRolePermissions(@PathVariable String roleName) {
        Role role = Role.valueOf(roleName.toUpperCase());
        Set<PermissionResponse> permissions = permissionService.getPermissionsByRole(role).stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toSet());
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/roles/{roleName}/users")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String roleName) {
        Role role = Role.valueOf(roleName.toUpperCase());
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    @PostMapping("/roles/{roleName}/permissions")
    public ResponseEntity<Map<String, String>> assignPermissionToRole(
            @PathVariable String roleName,
            @Valid @RequestBody AssignPermissionRequest request) {
        Role role = Role.valueOf(roleName.toUpperCase());
        permissionService.assignPermissionToRole(role, request.getPermissionCode());
        return ResponseEntity.ok(Map.of("message", "Permission assigned successfully"));
    }

    @DeleteMapping("/roles/{roleName}/permissions/{permissionCode}")
    public ResponseEntity<Map<String, String>> removePermissionFromRole(
            @PathVariable String roleName,
            @PathVariable String permissionCode) {
        Role role = Role.valueOf(roleName.toUpperCase());
        permissionService.removePermissionFromRole(role, permissionCode);
        return ResponseEntity.ok(Map.of("message", "Permission removed successfully"));
    }

    // ========== PERMISSION - ROLE MANAGEMENT ==========
    
    @GetMapping("/permissions/{permissionCode}/roles")
    public ResponseEntity<List<RolePermissionResponse>> getRolesByPermission(@PathVariable String permissionCode) {
        return ResponseEntity.ok(permissionService.getRolesByPermission(permissionCode));
    }

    @GetMapping("/permissions/{permissionCode}/users")
    public ResponseEntity<List<UserResponse>> getUsersByPermission(@PathVariable String permissionCode) {
        return ResponseEntity.ok(userService.getUsersByPermission(permissionCode));
    }

    // ========== USER - ROLE MANAGEMENT ==========
    
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<RolePermissionResponse>> getUserRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserRoles(userId));
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<Set<PermissionResponse>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserPermissions(userId));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, String>> assignRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {
        Role role = Role.valueOf(request.getRoleName().toUpperCase());
        userService.assignRoleToUser(userId, role);
        return ResponseEntity.ok(Map.of("message", "Role assigned successfully"));
    }
}

