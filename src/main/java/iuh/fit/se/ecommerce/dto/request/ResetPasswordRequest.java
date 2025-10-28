package iuh.fit.se.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 4, max = 10)
    private String otp;

    @NotBlank
    @Size(min = 6, max = 100)
    private String newPassword;
}
