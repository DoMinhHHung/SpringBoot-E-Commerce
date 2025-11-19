package iuh.fit.se.ecommerce.entity;

import iuh.fit.se.ecommerce.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RolePermission.RolePermissionId.class)
public class RolePermission {
    
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", length = 50)
    private Role role;

    @Id
    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RolePermissionId implements Serializable {
        private Role role;
        private Long permission;
    }
}

