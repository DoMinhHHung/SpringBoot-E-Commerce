package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ForgotPasswordRequest;
import iuh.fit.se.ecommerce.dto.request.LoginRequest;
import iuh.fit.se.ecommerce.dto.request.RegisterRequest;
import iuh.fit.se.ecommerce.dto.request.ResetPasswordRequest;
import iuh.fit.se.ecommerce.dto.response.LoginResponse;
import iuh.fit.se.ecommerce.dto.response.RegisterResponse;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        UserResponse resp = authService.User_Register(req);
        String message = "Đăng ký thành công. Vui lòng kiểm tra email của bạn để xác thực tài khoản.";
        RegisterResponse registerResponse = new RegisterResponse(resp, message);
        return ResponseEntity.ok(registerResponse);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id){
        UserResponse resp = authService.getById(id);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok("Tài khoản đã được kích hoạt thành công.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@RequestParam String token) {
        return ResponseEntity.ok(authService.refreshToken(token));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(Map.of("message", "Mã OTP đã được gửi. Vui lòng kiểm tra email của bạn hoặc thư spam!."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(Map.of("message", "Đặt lại mật khẩu thành công."));
    }
}
