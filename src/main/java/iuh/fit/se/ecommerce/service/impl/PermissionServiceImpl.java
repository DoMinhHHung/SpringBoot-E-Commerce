package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.mapper.PermissionMapper;
import iuh.fit.se.ecommerce.dto.response.PermissionResponse;
import iuh.fit.se.ecommerce.dto.response.RolePermissionResponse;
import iuh.fit.se.ecommerce.entity.Permission;
import iuh.fit.se.ecommerce.entity.RolePermission;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.PermissionRepository;
import iuh.fit.se.ecommerce.repository.RolePermissionRepository;
import iuh.fit.se.ecommerce.service.interfaces.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public boolean hasPermission(User user, String permissionCode) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        return getPermissionCodesByRole(user.getRole()).contains(permissionCode);
    }

    @Override
    public boolean hasAnyPermission(User user, String... permissionCodes) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        Set<String> userPermissions = getPermissionCodesByRole(user.getRole());
        for (String code : permissionCodes) {
            if (userPermissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAllPermissions(User user, String... permissionCodes) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        Set<String> userPermissions = getPermissionCodesByRole(user.getRole());
        for (String code : permissionCodes) {
            if (!userPermissions.contains(code)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<Permission> getPermissionsByRole(Role role) {
        return permissionRepository.findByRole(role);
    }

    @Override
    public Set<String> getPermissionCodesByRole(Role role) {
        return getPermissionsByRole(role).stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void assignPermissionToRole(Role role, String permissionCode) {
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Permission not found: " + permissionCode));
        
        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .build();
            rolePermissionRepository.save(rolePermission);
        }
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Role role, String permissionCode) {
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Permission not found: " + permissionCode));
        
        rolePermissionRepository.findByRole(role).stream()
                .filter(rp -> rp.getPermission().equals(permission))
                .forEach(rolePermissionRepository::delete);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse getPermissionByCode(String code) {
        Permission permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Permission not found: " + code));
        return permissionMapper.toResponse(permission);
    }

    @Override
    public List<RolePermissionResponse> getAllRolesWithPermissions() {
        return List.of(Role.values()).stream()
                .map(this::getRoleWithPermissions)
                .collect(Collectors.toList());
    }

    @Override
    public RolePermissionResponse getRoleWithPermissions(Role role) {
        Set<Permission> permissions = getPermissionsByRole(role);
        return RolePermissionResponse.builder()
                .roleName(role.name())
                .roleCode(role.name())
                .permissions(permissions.stream()
                        .map(permissionMapper::toResponse)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public List<RolePermissionResponse> getRolesByPermission(String permissionCode) {
        Set<Role> roles = rolePermissionRepository.findRolesByPermissionCode(permissionCode);
        return roles.stream()
                .map(this::getRoleWithPermissions)
                .collect(Collectors.toList());
    }
}

