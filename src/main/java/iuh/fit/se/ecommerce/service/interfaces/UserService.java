package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.ChangePasswordRequest;
import iuh.fit.se.ecommerce.dto.request.UpdateProfileRequest;
import iuh.fit.se.ecommerce.dto.response.UserResponse;

public interface UserService {
    void changePassword(String email, ChangePasswordRequest req);
    UserResponse updateProfile(String email, UpdateProfileRequest req);
    UserResponse getByEmail(String email);
}
