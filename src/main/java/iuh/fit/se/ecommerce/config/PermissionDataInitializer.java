package iuh.fit.se.ecommerce.config;

import iuh.fit.se.ecommerce.entity.Permission;
import iuh.fit.se.ecommerce.entity.RolePermission;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.repository.PermissionRepository;
import iuh.fit.se.ecommerce.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Order(1) // Run after DataInitializer
public class PermissionDataInitializer implements CommandLineRunner {
    
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        // 1. Tạo Permissions
        createPermissions();
        
        // 2. Gán permissions cho ADMIN role
        assignPermissionsToAdmin();
        
        // 3. Gán permissions cho EDITOR role (có thể chỉnh sửa products, promotions)
        assignPermissionsToEditor();
        
        // 4. USER role không có permissions đặc biệt (chỉ xem)
    }

    private void createPermissions() {
        // Product permissions
        createPermissionIfNotExists("PRODUCT_CREATE", "Create Product", "PRODUCT", "CREATE");
        createPermissionIfNotExists("PRODUCT_UPDATE", "Update Product", "PRODUCT", "UPDATE");
        createPermissionIfNotExists("PRODUCT_DELETE", "Delete Product", "PRODUCT", "DELETE");
        createPermissionIfNotExists("PRODUCT_VIEW", "View Product", "PRODUCT", "VIEW");
        
        // Order permissions
        createPermissionIfNotExists("ORDER_VIEW", "View Orders", "ORDER", "VIEW");
        createPermissionIfNotExists("ORDER_UPDATE", "Update Order Status", "ORDER", "UPDATE");
        
        // Transaction permissions
        createPermissionIfNotExists("TRANSACTION_VIEW", "View Transactions", "TRANSACTION", "VIEW");
        createPermissionIfNotExists("TRANSACTION_SUMMARY", "View Transaction Summary", "TRANSACTION", "SUMMARY");
        
        // Promotion permissions
        createPermissionIfNotExists("PROMOTION_CREATE", "Create Promotion", "PROMOTION", "CREATE");
        createPermissionIfNotExists("PROMOTION_UPDATE", "Update Promotion", "PROMOTION", "UPDATE");
        createPermissionIfNotExists("PROMOTION_DELETE", "Delete Promotion", "PROMOTION", "DELETE");
        
        // Support permissions
        createPermissionIfNotExists("SUPPORT_VIEW_PENDING", "View Pending Support", "SUPPORT", "VIEW_PENDING");
        createPermissionIfNotExists("SUPPORT_JOIN", "Join Support Session", "SUPPORT", "JOIN");
        createPermissionIfNotExists("SUPPORT_SEND", "Send Support Message", "SUPPORT", "SEND");
        createPermissionIfNotExists("SUPPORT_CLOSE", "Close Support Session", "SUPPORT", "CLOSE");
    }

    private void createPermissionIfNotExists(String code, String name, String resource, String action) {
        if (!permissionRepository.existsByCode(code)) {
            Permission permission = Permission.builder()
                    .code(code)
                    .name(name)
                    .resource(resource)
                    .action(action)
                    .description(name)
                    .build();
            permissionRepository.save(permission);
        }
    }

    private void assignPermissionsToAdmin() {
        // ADMIN có tất cả permissions
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        
        for (Permission permission : allPermissions) {
            if (!rolePermissionRepository.existsByRoleAndPermission(Role.ADMIN, permission)) {
                RolePermission rolePermission = RolePermission.builder()
                        .role(Role.ADMIN)
                        .permission(permission)
                        .build();
                rolePermissionRepository.save(rolePermission);
            }
        }
    }

    private void assignPermissionsToEditor() {
        // EDITOR có quyền quản lý products và promotions
        Set<String> editorPermissionCodes = Set.of(
                "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE", "PRODUCT_VIEW",
                "PROMOTION_CREATE", "PROMOTION_UPDATE", "PROMOTION_DELETE"
        );
        
        Set<Permission> editorPermissions = permissionRepository.findAll().stream()
                .filter(p -> editorPermissionCodes.contains(p.getCode()))
                .collect(Collectors.toSet());
        
        for (Permission permission : editorPermissions) {
            if (!rolePermissionRepository.existsByRoleAndPermission(Role.EDITOR, permission)) {
                RolePermission rolePermission = RolePermission.builder()
                        .role(Role.EDITOR)
                        .permission(permission)
                        .build();
                rolePermissionRepository.save(rolePermission);
            }
        }
    }
}

