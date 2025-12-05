package iuh.fit.se.ecommerce.config;

import iuh.fit.se.ecommerce.service.impl.CustomOAuth2UserService;
import iuh.fit.se.ecommerce.service.impl.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/oauth2/callback",
                                "/login/**",
                                "/register/**",
                                "/error",
                                "/verify/**",
                                "/forgot/**",
                                "/reset/**",
                                "/css/**",
                                "/js/**",
                                "/fragments/**",
                                "/",
                                "/index.html",
                                "/cart",
                                "/cart.html",
                                "/login.html",
                                "/register.html",
                                "/product-detail.html",
                                "/profile.html",
                                "/promotions.html",
                                "/products.html",
                                "/forgot-password.html",
                                "/payment-success.html",
                                "/payment-cancel.html",
                                "/checkout.html",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs/swagger-config",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/ws/**")
                        .permitAll()

                        // PayOS webhook callback (public)
                        .requestMatchers(HttpMethod.POST, "/api/payments/payos-callback").permitAll()

                        // Payment endpoints (authenticated)
                        .requestMatchers(HttpMethod.POST, "/api/payments/create").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/status/**").authenticated()


                        .requestMatchers(HttpMethod.GET, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/support/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/support/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Products: allow GET to public, restrict modifications to ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/promotions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/promotions/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated())
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login.html")
                        .failureUrl("/login.html?error=true")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler))
                .httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
