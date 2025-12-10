package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.config.JwtTokenProvider;
import iuh.fit.se.ecommerce.dto.mapper.UserMapper;
import iuh.fit.se.ecommerce.dto.request.ForgotPasswordRequest;
import iuh.fit.se.ecommerce.dto.request.LoginRequest;
import iuh.fit.se.ecommerce.dto.request.RegisterRequest;
import iuh.fit.se.ecommerce.dto.request.ResetPasswordRequest;
import iuh.fit.se.ecommerce.dto.response.LoginResponse;
import iuh.fit.se.ecommerce.dto.response.UserResponse;
import iuh.fit.se.ecommerce.entity.Otp;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.VerificationToken;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.OtpRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import iuh.fit.se.ecommerce.repository.VerificationTokenRepository;
import iuh.fit.se.ecommerce.event.UserRegisteredEvent;
import iuh.fit.se.ecommerce.service.interfaces.AuthService;
import iuh.fit.se.ecommerce.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final OtpRepository otpRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Override
    @Transactional
    public UserResponse User_Register(RegisterRequest req) {
        if(userRepository.existsByEmail(req.getEmail())){throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);}
        if(userRepository.existsByPhone(req.getPhone())){throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);}

        User user = userMapper.toEntity(req);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        user = userRepository.save(user);

        // Publish event for statistics audit
        try {
            eventPublisher.publishEvent(new UserRegisteredEvent(this, user));
        } catch (Exception ex) {
            // Log but don't fail registration
            System.err.println("Failed to publish user registered event: " + ex.getMessage());
        }

        String token = UUID.randomUUID().toString();
        VerificationToken vToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryAt(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepository.save(vToken);

        String verifyUrl = "https://gigatech-umxz.onrender.com/api/auth/verify?token=" + token;
        String subject = "Xác thực tài khoản E-Commerce";
        String body = "Chào " + user.getFullName() + ",\n\n"
                + "Click vào link dưới đây để kích hoạt tài khoản của bạn:\n"
                + verifyUrl + "\n\nLink có hiệu lực trong 15 phút.";
        try {
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            System.err.println("Failed to send verification email: " + ex.getMessage());
        }

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public void verifyAccount(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (vToken.isExpired()) throw new AppException(ErrorCode.INVALID_TOKEN);
        User user = vToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        tokenRepository.delete(vToken);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.isEnabled()) {
            throw new AppException(ErrorCode.NOT_AUTHENTICATED);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();

        } catch (AuthenticationException ex) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken))
            throw new AppException(ErrorCode.INVALID_TOKEN);

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newAccess = jwtTokenProvider.generateAccessToken(
                new UsernamePasswordAuthenticationToken(email, null)
        );
        String newRefresh = jwtTokenProvider.generateRefreshToken(email);

        return LoginResponse.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .tokenType("Bearer")
                .build();
    }

    private String generate6DigitsOtp() {
        SecureRandom rnd = new SecureRandom();
        int number = 100000 + rnd.nextInt(900000);
        return String.valueOf(number);
    }
    private static final int OTP_EXP_MINUTES = 10;
    @Override
    public void forgotPassword(ForgotPasswordRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        userRepository.findByEmail(email).ifPresent(user ->{
            String code = generate6DigitsOtp();
            Otp otp = Otp.builder()
                    .email(email)
                    .code(code)
                    .expiryAt(LocalDateTime.now().plusMinutes(OTP_EXP_MINUTES))
                    .used(false)
                    .build();
            otpRepository.save(otp);

            String subject = "OTP đặt lại mật khẩu E-Commerce (Hết hạn sau " + OTP_EXP_MINUTES + " phút)";
            String body = "Xin chào " + (user.getFullName() == null ? "" : user.getFullName()) + ",\n\n"
                    + "Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu. Mã OTP của bạn là:\n\n"
                    + code + "\n\n"
                    + "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n"
                    + "Mã sẽ hết hạn vào: " + otp.getExpiryAt() + "\n\n"
                    + "Thân,\nE-Commerce Team";

            emailService.sendEmail(email, subject, body);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String code = req.getOtp().trim();

        Otp otp = otpRepository.findTopByEmailAndCodeAndUsedFalseOrderByIdDesc(email, code)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP, "OTP không đúng hoặc đã dùng"));

        if (otp.getExpiryAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_OTP, "OTP đã hết hạn");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        otp.setUsed(true);
        otpRepository.save(otp);
    }
}
