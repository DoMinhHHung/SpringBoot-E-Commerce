package iuh.fit.se.ecommerce.config;

import iuh.fit.se.ecommerce.entity.Permission;
import iuh.fit.se.ecommerce.entity.RolePermission;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.repository.PermissionRepository;
import iuh.fit.se.ecommerce.repository.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class PermissionDataInitializer implements CommandLineRunner {
    
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        createPermissions();
        
        assignPermissionsToAdmin();
        
        assignPermissionsToEditor();
    }

    private void createPermissions() {
        createPermissionIfNotExists("PRODUCT_CREATE", "Create Product", "PRODUCT", "CREATE");
        createPermissionIfNotExists("PRODUCT_UPDATE", "Update Product", "PRODUCT", "UPDATE");
        createPermissionIfNotExists("PRODUCT_DELETE", "Delete Product", "PRODUCT", "DELETE");
        createPermissionIfNotExists("PRODUCT_VIEW", "View Product", "PRODUCT", "VIEW");
        
        createPermissionIfNotExists("ORDER_VIEW", "View Orders", "ORDER", "VIEW");
        createPermissionIfNotExists("ORDER_UPDATE", "Update Order Status", "ORDER", "UPDATE");
        
        createPermissionIfNotExists("TRANSACTION_VIEW", "View Transactions", "TRANSACTION", "VIEW");
        createPermissionIfNotExists("TRANSACTION_SUMMARY", "View Transaction Summary", "TRANSACTION", "SUMMARY");
        
        createPermissionIfNotExists("PROMOTION_CREATE", "Create Promotion", "PROMOTION", "CREATE");
        createPermissionIfNotExists("PROMOTION_UPDATE", "Update Promotion", "PROMOTION", "UPDATE");
        createPermissionIfNotExists("PROMOTION_DELETE", "Delete Promotion", "PROMOTION", "DELETE");
        
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
        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
        log.info("PermissionDataInitializer: Found {} total permissions", allPermissions.size());
        
        int assignedCount = 0;
        int existingCount = 0;
        
        for (Permission permission : allPermissions) {
            if (!rolePermissionRepository.existsByRoleAndPermission(Role.ADMIN, permission)) {
                RolePermission rolePermission = RolePermission.builder()
                        .role(Role.ADMIN)
                        .permission(permission)
                        .build();
                rolePermissionRepository.save(rolePermission);
                assignedCount++;
                log.debug("PermissionDataInitializer: Assigned permission {} to ADMIN role", permission.getCode());
            } else {
                existingCount++;
            }
        }
        
        log.info("PermissionDataInitializer: ADMIN role - {} new permissions assigned, {} already existed", 
            assignedCount, existingCount);
        log.info("PermissionDataInitializer: ADMIN role now has {} total permissions", 
            rolePermissionRepository.findByRole(Role.ADMIN).size());
    }

    private void assignPermissionsToEditor() {
        Set<String> editorPermissionCodes = Set.of(
                "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE", "PRODUCT_VIEW",
                "PROMOTION_CREATE", "PROMOTION_UPDATE", "PROMOTION_DELETE"
        );
        
        Set<Permission> editorPermissions = permissionRepository.findAll().stream()
                .filter(p -> editorPermissionCodes.contains(p.getCode()))
                .collect(Collectors.toSet());
        
        log.info("PermissionDataInitializer: Found {} permissions for EDITOR role", editorPermissions.size());
        
        int assignedCount = 0;
        int existingCount = 0;
        
        for (Permission permission : editorPermissions) {
            if (!rolePermissionRepository.existsByRoleAndPermission(Role.EDITOR, permission)) {
                RolePermission rolePermission = RolePermission.builder()
                        .role(Role.EDITOR)
                        .permission(permission)
                        .build();
                rolePermissionRepository.save(rolePermission);
                assignedCount++;
                log.debug("PermissionDataInitializer: Assigned permission {} to EDITOR role", permission.getCode());
            } else {
                existingCount++;
            }
        }
        
        log.info("PermissionDataInitializer: EDITOR role - {} new permissions assigned, {} already existed", 
            assignedCount, existingCount);
        log.info("PermissionDataInitializer: EDITOR role now has {} total permissions", 
            rolePermissionRepository.findByRole(Role.EDITOR).size());
    }
}

