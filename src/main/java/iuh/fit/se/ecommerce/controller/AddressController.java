package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.AddressRequest;
import iuh.fit.se.ecommerce.dto.response.AddressResponse;
import iuh.fit.se.ecommerce.service.interfaces.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AddressResponse> addresses = addressService.getUserAddresses(userDetails.getUsername());
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AddressResponse address = addressService.getAddressById(id, userDetails.getUsername());
        return ResponseEntity.ok(address);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.createAddress(request, userDetails.getUsername());
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse address = addressService.updateAddress(id, request, userDetails.getUsername());
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        addressService.deleteAddress(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/set-default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AddressResponse address = addressService.setDefaultAddress(id, userDetails.getUsername());
        return ResponseEntity.ok(address);
    }
}

