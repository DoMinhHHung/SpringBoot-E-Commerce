package iuh.fit.se.ecommerce.service.impl;

import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.Permission;
import iuh.fit.se.ecommerce.repository.PermissionRepository;
import iuh.fit.se.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("CustomUserDetailsService: Loading user by username: {}", username);

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.error("CustomUserDetailsService: User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });

        log.info("CustomUserDetailsService: User found - email={}, role={}, enabled={}",
            user.getEmail(), user.getRole(), user.isEnabled());

        // Lấy permissions từ role của user
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        // Thêm role authority
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            
            // Thêm permission authorities
            Set<org.springframework.security.core.GrantedAuthority> permissionAuthorities = 
                permissionRepository.findByRole(user.getRole()).stream()
                    .map(p -> new SimpleGrantedAuthority(p.getCode()))
                    .collect(java.util.stream.Collectors.toSet());
            authorities.addAll(permissionAuthorities);
        }

        String password = user.getPassword();
        if (password == null) {
            password = "";
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}
