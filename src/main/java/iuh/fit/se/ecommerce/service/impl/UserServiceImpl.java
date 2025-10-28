package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.dto.mapper.UserMapper;
import iuh.fit.se.ecommerce.dto.request.ChangePasswordRequest;
import iuh.fit.se.ecommerce.dto.request.UpdateProfileRequest;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


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
}
