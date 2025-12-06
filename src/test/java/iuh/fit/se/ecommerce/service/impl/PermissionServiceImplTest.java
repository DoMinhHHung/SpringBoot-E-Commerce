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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private User testUser;
    private Permission permission1;
    private Permission permission2;
    private Permission permission3;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .build();

        permission1 = Permission.builder()
                .id(1L)
                .code("PRODUCT_CREATE")
                .name("Create Product")
                .description("Permission to create products")
                .resource("PRODUCT")
                .action("CREATE")
                .build();

        permission2 = Permission.builder()
                .id(2L)
                .code("PRODUCT_VIEW")
                .name("View Product")
                .description("Permission to view products")
                .resource("PRODUCT")
                .action("VIEW")
                .build();

        permission3 = Permission.builder()
                .id(3L)
                .code("ORDER_UPDATE")
                .name("Update Order")
                .description("Permission to update orders")
                .resource("ORDER")
                .action("UPDATE")
                .build();
    }

    @Test
    void hasPermission_UserHasPermission_ReturnsTrue() {
        // Given
        when(permissionRepository.findByRole(Role.ADMIN))
                .thenReturn(new HashSet<>(Arrays.asList(permission1, permission2)));

        // When
        boolean result = permissionService.hasPermission(testUser, "PRODUCT_CREATE");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasPermission_UserDoesNotHavePermission_ReturnsFalse() {
        // Given
        when(permissionRepository.findByRole(Role.ADMIN))
                .thenReturn(new HashSet<>(Arrays.asList(permission2)));

        // When
        boolean result = permissionService.hasPermission(testUser, "PRODUCT_CREATE");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasPermission_NullUser_ReturnsFalse() {
        // When
        boolean result = permissionService.hasPermission(null, "PRODUCT_CREATE");

        // Then
        assertThat(result).isFalse();
        verify(permissionRepository, never()).findByRole(any());
    }

    @Test
    void hasPermission_UserWithNullRole_ReturnsFalse() {
        // Given
        testUser.setRole(null);

        // When
        boolean result = permissionService.hasPermission(testUser, "PRODUCT_CREATE");

        // Then
        assertThat(result).isFalse();
        verify(permissionRepository, never()).findByRole(any());
    }

    @Test
    void hasAnyPermission_UserHasOnePermission_ReturnsTrue() {
        // Given
        when(permissionRepository.findByRole(Role.ADMIN))
                .thenReturn(new HashSet<>(Arrays.asList(permission1, permission2)));

        // When
        boolean result = permissionService.hasAnyPermission(testUser, "PRODUCT_CREATE", "ORDER_DELETE");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAnyPermission_UserHasNoPermissions_ReturnsFalse() {
        // Given
        when(permissionRepository.findByRole(Role.ADMIN))
                .thenReturn(new HashSet<>(Arrays.asList(permission3)));

        // When
        boolean result = permissionService.hasAnyPermission(testUser, "PRODUCT_CREATE", "PRODUCT_VIEW");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAnyPermission_NullUser_ReturnsFalse() {
        // When
        boolean result = permissionService.hasAnyPermission(null, "PRODUCT_CREATE");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAllPermissions_UserHasAllPermissions_ReturnsTrue() {
        // Given
        when(permissionRepository.findByRole(Role.ADMIN))
                .thenReturn(new HashSet<>(Arrays.asList(permission1, permission2, permission3)));

        // When
        boolean result = permissionService.hasAllPermissions(testUser, "PRODUCT_CREATE", "PRODUCT_VIEW");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasAllPermissions_UserMissingOnePermission_ReturnsFalse() {
        // Given
        when(permissionRepository.findByRole(Role.ADMIN))
                .thenReturn(new HashSet<>(Arrays.asList(permission1)));

        // When
        boolean result = permissionService.hasAllPermissions(testUser, "PRODUCT_CREATE", "PRODUCT_VIEW");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void hasAllPermissions_NullUser_ReturnsFalse() {
        // When
        boolean result = permissionService.hasAllPermissions(null, "PRODUCT_CREATE");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getPermissionsByRole_Success() {
        // Given
        Set<Permission> permissions = new HashSet<>(Arrays.asList(permission1, permission2));
        when(permissionRepository.findByRole(Role.ADMIN)).thenReturn(permissions);

        // When
        Set<Permission> result = permissionService.getPermissionsByRole(Role.ADMIN);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(permission1, permission2);
        verify(permissionRepository).findByRole(Role.ADMIN);
    }

    @Test
    void getPermissionCodesByRole_Success() {
        // Given
        Set<Permission> permissions = new HashSet<>(Arrays.asList(permission1, permission2));
        when(permissionRepository.findByRole(Role.ADMIN)).thenReturn(permissions);

        // When
        Set<String> result = permissionService.getPermissionCodesByRole(Role.ADMIN);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains("PRODUCT_CREATE", "PRODUCT_VIEW");
        verify(permissionRepository).findByRole(Role.ADMIN);
    }

    @Test
    void assignPermissionToRole_NewAssignment_Success() {
        // Given
        when(permissionRepository.findByCode("PRODUCT_CREATE")).thenReturn(Optional.of(permission1));
        when(rolePermissionRepository.existsByRoleAndPermission(Role.STAFF, permission1)).thenReturn(false);
        when(rolePermissionRepository.save(any(RolePermission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        permissionService.assignPermissionToRole(Role.STAFF, "PRODUCT_CREATE");

        // Then
        verify(permissionRepository).findByCode("PRODUCT_CREATE");
        verify(rolePermissionRepository).existsByRoleAndPermission(Role.STAFF, permission1);
        verify(rolePermissionRepository).save(any(RolePermission.class));
    }

    @Test
    void assignPermissionToRole_AlreadyAssigned_DoesNotDuplicate() {
        // Given
        when(permissionRepository.findByCode("PRODUCT_CREATE")).thenReturn(Optional.of(permission1));
        when(rolePermissionRepository.existsByRoleAndPermission(Role.STAFF, permission1)).thenReturn(true);

        // When
        permissionService.assignPermissionToRole(Role.STAFF, "PRODUCT_CREATE");

        // Then
        verify(permissionRepository).findByCode("PRODUCT_CREATE");
        verify(rolePermissionRepository).existsByRoleAndPermission(Role.STAFF, permission1);
        verify(rolePermissionRepository, never()).save(any());
    }

    @Test
    void assignPermissionToRole_PermissionNotFound_ThrowsException() {
        // Given
        when(permissionRepository.findByCode("INVALID_CODE")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> permissionService.assignPermissionToRole(Role.STAFF, "INVALID_CODE"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND)
                .hasMessageContaining("Permission not found");
        verify(rolePermissionRepository, never()).save(any());
    }

    @Test
    void removePermissionFromRole_Success() {
        // Given
        RolePermission rolePermission = RolePermission.builder()
                .id(1L)
                .role(Role.STAFF)
                .permission(permission1)
                .build();

        when(permissionRepository.findByCode("PRODUCT_CREATE")).thenReturn(Optional.of(permission1));
        when(rolePermissionRepository.findByRole(Role.STAFF))
                .thenReturn(Arrays.asList(rolePermission));

        // When
        permissionService.removePermissionFromRole(Role.STAFF, "PRODUCT_CREATE");

        // Then
        verify(permissionRepository).findByCode("PRODUCT_CREATE");
        verify(rolePermissionRepository).delete(rolePermission);
    }

    @Test
    void removePermissionFromRole_PermissionNotFound_ThrowsException() {
        // Given
        when(permissionRepository.findByCode("INVALID_CODE")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> permissionService.removePermissionFromRole(Role.STAFF, "INVALID_CODE"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
        verify(rolePermissionRepository, never()).delete(any());
    }

    @Test
    void getAllPermissions_Success() {
        // Given
        List<Permission> permissions = Arrays.asList(permission1, permission2, permission3);
        PermissionResponse response1 = PermissionResponse.builder().code("PRODUCT_CREATE").build();
        PermissionResponse response2 = PermissionResponse.builder().code("PRODUCT_VIEW").build();
        PermissionResponse response3 = PermissionResponse.builder().code("ORDER_UPDATE").build();

        when(permissionRepository.findAll()).thenReturn(permissions);
        when(permissionMapper.toResponse(permission1)).thenReturn(response1);
        when(permissionMapper.toResponse(permission2)).thenReturn(response2);
        when(permissionMapper.toResponse(permission3)).thenReturn(response3);

        // When
        List<PermissionResponse> result = permissionService.getAllPermissions();

        // Then
        assertThat(result).hasSize(3);
        verify(permissionRepository).findAll();
    }

    @Test
    void getPermissionByCode_Success() {
        // Given
        PermissionResponse response = PermissionResponse.builder()
                .code("PRODUCT_CREATE")
                .name("Create Product")
                .build();

        when(permissionRepository.findByCode("PRODUCT_CREATE")).thenReturn(Optional.of(permission1));
        when(permissionMapper.toResponse(permission1)).thenReturn(response);

        // When
        PermissionResponse result = permissionService.getPermissionByCode("PRODUCT_CREATE");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("PRODUCT_CREATE");
        verify(permissionRepository).findByCode("PRODUCT_CREATE");
    }

    @Test
    void getPermissionByCode_NotFound_ThrowsException() {
        // Given
        when(permissionRepository.findByCode("INVALID_CODE")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> permissionService.getPermissionByCode("INVALID_CODE"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND);
    }

    @Test
    void getAllRolesWithPermissions_Success() {
        // Given
        Set<Permission> adminPermissions = new HashSet<>(Arrays.asList(permission1, permission2));
        Set<Permission> staffPermissions = new HashSet<>(Arrays.asList(permission2));

        when(permissionRepository.findByRole(Role.ADMIN)).thenReturn(adminPermissions);
        when(permissionRepository.findByRole(Role.STAFF)).thenReturn(staffPermissions);
        when(permissionRepository.findByRole(Role.CUSTOMER)).thenReturn(new HashSet<>());
        
        when(permissionMapper.toResponse(any())).thenAnswer(invocation -> {
            Permission p = invocation.getArgument(0);
            return PermissionResponse.builder().code(p.getCode()).build();
        });

        // When
        List<RolePermissionResponse> result = permissionService.getAllRolesWithPermissions();

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void getRoleWithPermissions_Success() {
        // Given
        Set<Permission> permissions = new HashSet<>(Arrays.asList(permission1, permission2));
        PermissionResponse response1 = PermissionResponse.builder().code("PRODUCT_CREATE").build();
        PermissionResponse response2 = PermissionResponse.builder().code("PRODUCT_VIEW").build();

        when(permissionRepository.findByRole(Role.ADMIN)).thenReturn(permissions);
        when(permissionMapper.toResponse(permission1)).thenReturn(response1);
        when(permissionMapper.toResponse(permission2)).thenReturn(response2);

        // When
        RolePermissionResponse result = permissionService.getRoleWithPermissions(Role.ADMIN);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRoleName()).isEqualTo("ADMIN");
        assertThat(result.getRoleCode()).isEqualTo("ADMIN");
        assertThat(result.getPermissions()).hasSize(2);
    }

    @Test
    void getRolesByPermission_Success() {
        // Given
        Set<Role> roles = new HashSet<>(Arrays.asList(Role.ADMIN, Role.STAFF));
        
        when(rolePermissionRepository.findRolesByPermissionCode("PRODUCT_VIEW")).thenReturn(roles);
        when(permissionRepository.findByRole(any())).thenReturn(new HashSet<>());

        // When
        List<RolePermissionResponse> result = permissionService.getRolesByPermission("PRODUCT_VIEW");

        // Then
        assertThat(result).hasSize(2);
        verify(rolePermissionRepository).findRolesByPermissionCode("PRODUCT_VIEW");
    }
}