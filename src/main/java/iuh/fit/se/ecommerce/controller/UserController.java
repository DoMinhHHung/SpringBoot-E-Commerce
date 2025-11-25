package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ChangePasswordRequest;
import iuh.fit.se.ecommerce.dto.request.UpdateProfileRequest;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userDetails.getUsername(), req);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody UpdateProfileRequest req) {
        UserResponse updated = userService.updateProfile(userDetails.getUsername(), req);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse resp = userService.getByEmail(userDetails.getUsername());
        if (resp.isBanned()) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản của bạn đã bị chặn"));
        }
        return ResponseEntity.ok(resp);
    }
}