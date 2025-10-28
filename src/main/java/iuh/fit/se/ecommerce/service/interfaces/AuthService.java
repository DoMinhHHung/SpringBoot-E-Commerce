package iuh.fit.se.ecommerce.service.interfaces;

import iuh.fit.se.ecommerce.dto.request.*;
import iuh.fit.se.ecommerce.dto.response.*;
import iuh.fit.se.ecommerce.dto.response.UserResponse;

public interface AuthService {
    UserResponse User_Register(RegisterRequest req);
    UserResponse getById(Long id);
    void verifyAccount(String token);
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    void forgotPassword(ForgotPasswordRequest req);
    void resetPassword(ResetPasswordRequest req);
}
