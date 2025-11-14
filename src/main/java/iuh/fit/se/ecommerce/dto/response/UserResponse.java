package iuh.fit.se.ecommerce.dto.response;

import iuh.fit.se.ecommerce.entity.enums.AuthProvider;
import iuh.fit.se.ecommerce.entity.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private String phone;
    private String gender;
    private LocalDate dob;
    private AuthProvider authProvider;
    private Role role;
    private boolean enabled;
}
