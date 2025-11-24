package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.dto.request.ChangePasswordRequest;
import iuh.fit.se.ecommerce.dto.request.UpdateProfileRequest;
import iuh.fit.se.ecommerce.dto.response.PermissionResponse;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

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
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse resp = userService.getByEmail(userDetails.getUsername());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/permissions")
    public ResponseEntity<Set<PermissionResponse>> getMyPermissions(
            @AuthenticationPrincipal UserDetails userDetails) {
        var user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return ResponseEntity.ok(userService.getUserPermissions(user.getId()));
    }
}