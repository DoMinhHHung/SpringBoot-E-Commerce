package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.request.AddressRequest;
import iuh.fit.se.ecommerce.dto.response.AddressResponse;
import iuh.fit.se.ecommerce.entity.Address;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.AddressRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    public List<AddressResponse> getUserAddresses(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<Address> addresses = addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(user);
        return addresses.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AddressResponse getAddressById(Long addressId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Địa chỉ không tồn tại"));

        return mapToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Nếu set default, unset các address default khác
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
        }

        Address address = Address.builder()
                .label(request.getLabel())
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .province(request.getProvince())
                .ward(request.getWard())
                .detail(request.getDetail())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .latitude(request.getLatitude())      // Có thể null
                .longitude(request.getLongitude())    // Có thể null
                .user(user)
                .build();

        address = addressRepository.save(address);
        log.info("Created address {} for user {}", address.getId(), userEmail);

        return mapToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Địa chỉ không tồn tại"));

        // Nếu set default, unset các address default khác
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.isDefault()) {
            addressRepository.findByUserAndIsDefaultTrue(user)
                    .ifPresent(addr -> {
                        addr.setDefault(false);
                        addressRepository.save(addr);
                    });
        }

        address.setLabel(request.getLabel());
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setWard(request.getWard());
        address.setDetail(request.getDetail());
        address.setDefault(Boolean.TRUE.equals(request.getIsDefault()));
        address.setLatitude(request.getLatitude());      // Có thể null
        address.setLongitude(request.getLongitude());    // Có thể null

        address = addressRepository.save(address);
        log.info("Updated address {} for user {}", addressId, userEmail);

        return mapToResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Địa chỉ không tồn tại"));

        addressRepository.delete(address);
        log.info("Deleted address {} for user {}", addressId, userEmail);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findByIdAndUser(addressId, user)
                .orElseThrow(() -> new AppException(ErrorCode.BAD_REQUEST, "Địa chỉ không tồn tại"));

        // Unset các address default khác
        addressRepository.findByUserAndIsDefaultTrue(user)
                .ifPresent(addr -> {
                    addr.setDefault(false);
                    addressRepository.save(addr);
                });

        address.setDefault(true);
        address = addressRepository.save(address);

        return mapToResponse(address);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .label(address.getLabel())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .province(address.getProvince())
                .ward(address.getWard())
                .addressDetail(address.getDetail())
                .isDefault(address.isDefault())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .country("Vietnam")
                .build();
    }
}

