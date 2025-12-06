package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.response.AddressGeocodeResponse;
import iuh.fit.se.ecommerce.service.interfaces.NominatimService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/geocoding")
@RequiredArgsConstructor
public class GeocodingController {

    private final NominatimService nominatimService;

    @GetMapping("/reverse")
    public ResponseEntity<AddressGeocodeResponse> reverseGeocode(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng) {

        AddressGeocodeResponse result = nominatimService.reverseGeocode(lat, lng);
        return ResponseEntity.ok(result);
    }
}

