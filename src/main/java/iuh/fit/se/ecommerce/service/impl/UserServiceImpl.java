package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.mapper.PermissionMapper;
import iuh.fit.se.ecommerce.dto.mapper.UserMapper;
import iuh.fit.se.ecommerce.dto.request.ChangePasswordRequest;
import iuh.fit.se.ecommerce.dto.request.UpdateProfileRequest;
import iuh.fit.se.ecommerce.dto.response.PermissionResponse;
import iuh.fit.se.ecommerce.dto.response.RolePermissionResponse;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.EmailService;
import iuh.fit.se.ecommerce.service.interfaces.PermissionService;
import iuh.fit.se.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;
    private final EmailService emailService;


    @Override
    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD, "Mật khẩu cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (req.getFullname() != null) user.setFullName(req.getFullname());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getDob() != null) user.setDob(UserMapper.toLocalDate(req.getDob()));

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByPermission(String permissionCode) {
        // Get all roles that have this permission
        List<RolePermissionResponse> rolePermissions = permissionService.getRolesByPermission(permissionCode);
        Set<Role> roles = rolePermissions.stream()
                .map(rp -> Role.valueOf(rp.getRoleName()))
                .collect(Collectors.toSet());
        
        return roles.stream()
                .flatMap(role -> userRepository.findByRole(role).stream())
                .map(userMapper::toResponse)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<RolePermissionResponse> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        Role role = user.getRole();
        if (role == null) {
            return List.of();
        }
        
        return List.of(permissionService.getRoleWithPermissions(role));
    }

    @Override
    public Set<PermissionResponse> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        if (user.getRole() == null) {
            return Set.of();
        }
        
        return permissionService.getPermissionsByRole(user.getRole()).stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void banUser(Long userId, Long adminId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (user.isBanned()) return;
        user.setBanned(true);
        userRepository.save(user);

        try {
            String subject = "[E-Commerce] Tài khoản của bạn đã bị chặn";
            String body = "Xin chào " + (user.getFullName() == null ? "người dùng" : user.getFullName()) + ",\n\n"
                    + "Tài khoản của bạn đã bị chặn bởi quản trị viên." + (reason != null && !reason.isBlank() ? " Lý do: " + reason : "")
                    + "\n\nNếu bạn cho rằng đây là sai lầm, vui lòng liên hệ hỗ trợ.";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            System.err.println("Failed to send ban email: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void unbanUser(Long userId, Long adminId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isBanned()) return; // already not banned
        user.setBanned(false);
        userRepository.save(user);

        try {
            String subject = "[E-Commerce] Tài khoản của bạn đã được mở lại";
            String body = "Xin chào " + (user.getFullName() == null ? "người dùng" : user.getFullName()) + ",\n\n"
                    + "Tài khoản của bạn đã được mở lại bởi quản trị viên.";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            System.err.println("Failed to send unban email: " + ex.getMessage());
        }
    }
}
