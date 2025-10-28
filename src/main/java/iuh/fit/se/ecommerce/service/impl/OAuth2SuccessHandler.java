package iuh.fit.se.ecommerce.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.ecommerce.config.JwtTokenProvider;
import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.exception.AppException;
import iuh.fit.se.ecommerce.exception.ErrorCode;
import iuh.fit.se.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

        private final JwtTokenProvider jwtTokenProvider;
        private final UserRepository userRepository;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                        Authentication authentication) throws IOException {
                DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
                String email = oauthUser.getAttribute("email");

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                String refreshToken = jwtTokenProvider.generateRefreshToken(email);
                response.setContentType("application/json");

                response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
                                "accessToken", accessToken,
                                "refreshToken", refreshToken,
                                "tokenType", "Bearer")));
                response.sendRedirect("/profile");
        }
}
