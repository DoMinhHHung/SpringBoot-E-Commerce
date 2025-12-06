package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.request.AddressRequest;
import iuh.fit.se.ecommerce.dto.response.AddressResponse;
import iuh.fit.se.ecommerce.entity.Address;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.AddressRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private Address testAddress;
    private AddressRequest addressRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .build();

        testAddress = Address.builder()
                .id(1L)
                .label("Home")
                .receiverName("John Doe")
                .receiverPhone("0123456789")
                .province("Ho Chi Minh")
                .ward("Ward 1")
                .detail("123 Main St")
                .isDefault(false)
                .latitude(10.762622)
                .longitude(106.660172)
                .user(testUser)
                .build();

        addressRequest = AddressRequest.builder()
                .label("Office")
                .receiverName("Jane Doe")
                .receiverPhone("0987654321")
                .province("Hanoi")
                .ward("Ward 2")
                .detail("456 Second St")
                .isDefault(false)
                .latitude(21.028511)
                .longitude(105.804817)
                .build();
    }

    @Test
    void getUserAddresses_Success() {
        // Given
        Address address2 = Address.builder()
                .id(2L)
                .label("Office")
                .receiverName("Jane Doe")
                .receiverPhone("0987654321")
                .province("Hanoi")
                .ward("Ward 2")
                .detail("456 Second St")
                .isDefault(true)
                .user(testUser)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserOrderByIsDefaultDescCreatedAtDesc(testUser))
                .thenReturn(Arrays.asList(address2, testAddress));

        // When
        List<AddressResponse> result = addressService.getUserAddresses("test@example.com");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLabel()).isEqualTo("Office");
        assertThat(result.get(0).getIsDefault()).isTrue();
        assertThat(result.get(1).getLabel()).isEqualTo("Home");
        assertThat(result.get(1).getIsDefault()).isFalse();
        verify(userRepository).findByEmail("test@example.com");
        verify(addressRepository).findByUserOrderByIsDefaultDescCreatedAtDesc(testUser);
    }

    @Test
    void getUserAddresses_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addressService.getUserAddresses("nonexistent@example.com"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(addressRepository, never()).findByUserOrderByIsDefaultDescCreatedAtDesc(any());
    }

    @Test
    void getAddressById_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAddress));

        // When
        AddressResponse result = addressService.getAddressById(1L, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLabel()).isEqualTo("Home");
        assertThat(result.getReceiverName()).isEqualTo("John Doe");
        assertThat(result.getProvince()).isEqualTo("Ho Chi Minh");
        verify(userRepository).findByEmail("test@example.com");
        verify(addressRepository).findByIdAndUser(1L, testUser);
    }

    @Test
    void getAddressById_AddressNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addressService.getAddressById(999L, "test@example.com"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Địa chỉ không tồn tại");
        verify(userRepository).findByEmail("test@example.com");
        verify(addressRepository).findByIdAndUser(999L, testUser);
    }

    @Test
    void createAddress_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        AddressResponse result = addressService.createAddress(addressRequest, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLabel()).isEqualTo("Office");
        assertThat(result.getReceiverName()).isEqualTo("Jane Doe");
        assertThat(result.getProvince()).isEqualTo("Hanoi");
        assertThat(result.getLatitude()).isEqualTo(21.028511);
        assertThat(result.getLongitude()).isEqualTo(105.804817);
        verify(userRepository).findByEmail("test@example.com");
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void createAddress_WithDefault_UnsetsOtherDefaults() {
        // Given
        Address existingDefaultAddress = Address.builder()
                .id(3L)
                .isDefault(true)
                .user(testUser)
                .build();

        addressRequest.setIsDefault(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserAndIsDefaultTrue(testUser))
                .thenReturn(Optional.of(existingDefaultAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AddressResponse result = addressService.createAddress(addressRequest, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        verify(addressRepository, times(2)).save(any(Address.class)); // Once for unset, once for new
        assertThat(existingDefaultAddress.isDefault()).isFalse();
    }

    @Test
    void createAddress_NullCoordinates_Success() {
        // Given
        addressRequest.setLatitude(null);
        addressRequest.setLongitude(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        AddressResponse result = addressService.createAddress(addressRequest, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitude()).isNull();
        assertThat(result.getLongitude()).isNull();
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void updateAddress_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AddressResponse result = addressService.updateAddress(1L, addressRequest, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLabel()).isEqualTo("Office");
        assertThat(result.getReceiverName()).isEqualTo("Jane Doe");
        assertThat(result.getProvince()).isEqualTo("Hanoi");
        verify(addressRepository).save(testAddress);
    }

    @Test
    void updateAddress_SetAsDefault_UnsetsOtherDefaults() {
        // Given
        Address existingDefaultAddress = Address.builder()
                .id(3L)
                .isDefault(true)
                .user(testUser)
                .build();

        addressRequest.setIsDefault(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByUserAndIsDefaultTrue(testUser))
                .thenReturn(Optional.of(existingDefaultAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AddressResponse result = addressService.updateAddress(1L, addressRequest, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        assertThat(existingDefaultAddress.isDefault()).isFalse();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void updateAddress_AddressNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addressService.updateAddress(999L, addressRequest, "test@example.com"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Địa chỉ không tồn tại");
    }

    @Test
    void deleteAddress_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAddress));
        doNothing().when(addressRepository).delete(testAddress);

        // When
        addressService.deleteAddress(1L, "test@example.com");

        // Then
        verify(addressRepository).delete(testAddress);
    }

    @Test
    void deleteAddress_AddressNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> addressService.deleteAddress(999L, "test@example.com"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Địa chỉ không tồn tại");
        verify(addressRepository, never()).delete(any());
    }

    @Test
    void setDefaultAddress_Success() {
        // Given
        Address existingDefaultAddress = Address.builder()
                .id(3L)
                .isDefault(true)
                .user(testUser)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByUserAndIsDefaultTrue(testUser))
                .thenReturn(Optional.of(existingDefaultAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AddressResponse result = addressService.setDefaultAddress(1L, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        assertThat(testAddress.isDefault()).isTrue();
        assertThat(existingDefaultAddress.isDefault()).isFalse();
        verify(addressRepository, times(2)).save(any(Address.class));
    }

    @Test
    void setDefaultAddress_NoExistingDefault_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(addressRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testAddress));
        when(addressRepository.findByUserAndIsDefaultTrue(testUser)).thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AddressResponse result = addressService.setDefaultAddress(1L, "test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsDefault()).isTrue();
        verify(addressRepository, times(1)).save(testAddress);
    }
}