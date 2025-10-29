package iuh.fit.se.ecommerce.config;

import iuh.fit.se.ecommerce.entity.User;
import iuh.fit.se.ecommerce.entity.enums.AuthProvider;
import iuh.fit.se.ecommerce.entity.enums.Role;
import iuh.fit.se.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
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
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        userRepository.findByEmail(adminEmail).ifPresentOrElse(u -> {
            if (u.getRole() != Role.ADMIN || !u.isEnabled()) {
                u.setRole(Role.ADMIN);
                u.setEnabled(true);
                userRepository.save(u);
            }
        }, () -> {
            User admin = User.builder()
                    .fullName(adminFullName == null || adminFullName.isBlank() ? "Administrator" : adminFullName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword == null ? "admin" : adminPassword))
                    .role(Role.ADMIN)
                    .authProvider(AuthProvider.LOCAL)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        });
    }
}
