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
            String roleAuthority = "ROLE_" + user.getRole().name();
            authorities.add(new SimpleGrantedAuthority(roleAuthority));
            log.info("CustomUserDetailsService: Added role authority: {}", roleAuthority);
            
            // Thêm permission authorities
            Set<Permission> permissions = permissionRepository.findByRole(user.getRole());
            log.info("CustomUserDetailsService: Found {} permissions for role: {}", permissions.size(), user.getRole());
            
            if (permissions.isEmpty()) {
                log.warn("CustomUserDetailsService: No permissions found for role: {} - This might cause access issues!", user.getRole());
            } else {
                log.debug("CustomUserDetailsService: Permissions: {}", 
                    permissions.stream().map(Permission::getCode).collect(Collectors.joining(", ")));
            }
            
            Set<GrantedAuthority> permissionAuthorities = permissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.getCode()))
                    .collect(Collectors.toSet());
            authorities.addAll(permissionAuthorities);
            log.info("CustomUserDetailsService: Total authorities after adding permissions: {}", authorities.size());
        } else {
            log.warn("CustomUserDetailsService: User {} has null role! No authorities will be granted.", username);
        }

        log.info("CustomUserDetailsService: Final authorities for user {}: [{}]", 
            username, 
            authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));

        String password = user.getPassword();
        if (password == null) {
            password = "";
        }

        boolean isDisabled = !user.isEnabled();
        if (isDisabled) {
            log.warn("CustomUserDetailsService: User {} is disabled!", username);
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .authorities(authorities)
                .disabled(isDisabled)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
        
        log.info("CustomUserDetailsService: UserDetails created for {} with {} authorities", 
            username, userDetails.getAuthorities().size());
        
        return userDetails;
    }
}
