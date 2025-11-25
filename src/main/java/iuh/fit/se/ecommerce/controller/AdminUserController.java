package iuh.fit.se.ecommerce.controller;

import iuh.fit.se.ecommerce.service.interfaces.UserService;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    @PostMapping("/{userId}/ban")
    public ResponseEntity<Map<String, String>> banUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long adminId = null;
        if (principal != null) {
            try {
                UserResponse admin = userService.getByEmail(principal.getUsername());
                adminId = admin.getId();
            } catch (Exception ignored) {
            }
        }

        if (adminId != null && adminId.equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể tự chặn chính mình"));
        }

        userService.banUser(userId, adminId, reason);
        return ResponseEntity.ok(Map.of("message", "User banned"));
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<Map<String, String>> unbanUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long adminId = null;
        if (principal != null) {
            try {
                UserResponse admin = userService.getByEmail(principal.getUsername());
                adminId = admin.getId();
            } catch (Exception ignored) {
            }
        }

        userService.unbanUser(userId, adminId);
        return ResponseEntity.ok(Map.of("message", "User unbanned"));
    }
}
