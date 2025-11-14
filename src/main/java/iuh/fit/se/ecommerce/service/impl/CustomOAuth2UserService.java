package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.AuthProvider;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .fullName(name)
                            .avatar(picture)
                            .authProvider(AuthProvider.GOOGLE)
                            .enabled(true)
                            .role(Role.USER)
                            .build();
                    return userRepository.save(newUser);
                });
        
        // Update avatar if user exists but doesn't have one, or if Google avatar is newer
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar(picture);
            userRepository.save(user);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}
