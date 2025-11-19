package iuh.fit.se.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignPermissionRequest {
    @NotBlank(message = "Permission code is required")
    private String permissionCode;
}

