package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.AddressRequest;
import iuh.fit.se.ecommerce.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getUserAddresses(String userEmail);
    AddressResponse getAddressById(Long addressId, String userEmail);
    AddressResponse createAddress(AddressRequest request, String userEmail);
    AddressResponse updateAddress(Long addressId, AddressRequest request, String userEmail);
    void deleteAddress(Long addressId, String userEmail);
    AddressResponse setDefaultAddress(Long addressId, String userEmail);
}

