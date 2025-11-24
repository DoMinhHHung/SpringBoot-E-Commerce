package iuh.fit.se.ecommerce.config;

import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.AuthProvider;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(0)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.fullname}")
    private String adminFullName;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("DataInitializer: Starting admin user initialization...");
        log.info("DataInitializer: adminEmail={}", adminEmail);
        
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("DataInitializer: adminEmail is null or blank, skipping initialization");
            return;
        }

        userRepository.findByEmail(adminEmail).ifPresentOrElse(u -> {
            log.info("DataInitializer: Admin user exists - email={}, role={}, enabled={}", 
                u.getEmail(), u.getRole(), u.isEnabled());
            
            if (u.getRole() != Role.ADMIN || !u.isEnabled()) {
                log.info("DataInitializer: Updating admin user - setting role=ADMIN, enabled=true");
                u.setRole(Role.ADMIN);
                u.setEnabled(true);
                userRepository.save(u);
                log.info("DataInitializer: Admin user updated successfully - email={}, role={}, enabled={}", 
                    u.getEmail(), u.getRole(), u.isEnabled());
            } else {
                log.info("DataInitializer: Admin user already has correct role and enabled status");
            }
        }, () -> {
            log.info("DataInitializer: Admin user not found, creating new admin user");
            User admin = User.builder()
                    .fullName(adminFullName == null || adminFullName.isBlank() ? "Administrator" : adminFullName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword == null ? "admin" : adminPassword))
                    .role(Role.ADMIN)
                    .authProvider(AuthProvider.LOCAL)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("DataInitializer: Admin user created successfully - email={}, role={}, enabled={}, fullName={}", 
                admin.getEmail(), admin.getRole(), admin.isEnabled(), admin.getFullName());
        });
        
        log.info("DataInitializer: Completed admin user initialization");
        log.info("========================================");
    }
}
