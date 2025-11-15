package iuh.fit.se.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String country;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private boolean isDefault;
}

