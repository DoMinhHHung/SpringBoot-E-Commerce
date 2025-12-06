package iuh.fit.se.ecommerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressGeocodeResponse {
    private String houseNumber;
    private String road;
    private String ward;
    private String district;
    private String province;
    private String country;
    private String countryCode;
    private String postcode;
    private String displayName;
    private String fullAddress;
}

