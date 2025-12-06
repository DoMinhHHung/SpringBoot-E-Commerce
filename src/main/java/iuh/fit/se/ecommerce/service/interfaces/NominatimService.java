package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.response.AddressGeocodeResponse;
import java.math.BigDecimal;

public interface NominatimService {
    AddressGeocodeResponse reverseGeocode(BigDecimal lat, BigDecimal lng);
}

