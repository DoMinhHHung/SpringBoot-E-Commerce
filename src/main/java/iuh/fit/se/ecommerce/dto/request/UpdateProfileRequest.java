package iuh.fit.se.ecommerce.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String fullname;

    @Size(max = 15)
    private String phone;

    private String gender;

    private String dob;
}
