package iuh.fit.se.ecommerce.config;

import iuh.fit.se.ecommerce.service.impl.CustomOAuth2UserService;
import iuh.fit.se.ecommerce.service.impl.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
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
                                "/logo/**",
                                "/images/**",
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
                                "/search-results.html",
                                "/forgot-password.html",
                                "/payment-success.html",
                                "/payment-cancel.html",
                                "/checkout.html",
                                "/orders.html",
                                "/notifications.html",
                                "/order-detail.html",
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
                        
                        // Products: allow GET to public (must be before any authenticated rules)
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        
                        // Promotions: allow GET to public
                        .requestMatchers(HttpMethod.GET, "/api/promotions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/promotions").permitAll()

                        // Site-wide public notifications (e.g., new product announcements)
                        .requestMatchers(HttpMethod.GET, "/api/site-notifications", "/api/site-notifications/**").permitAll()

                        // Payment endpoints (authenticated)
                        .requestMatchers(HttpMethod.POST, "/api/payments/create").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/payments/status/**").authenticated()

                        // Address endpoints (authenticated)
                        .requestMatchers("/api/addresses/**").authenticated()
                        
                        // Geocoding endpoints (authenticated)
                        .requestMatchers(HttpMethod.GET, "/api/geocoding/**").authenticated()

                        // Admin endpoints (require roles for page access)
                        .requestMatchers(HttpMethod.GET, "/admin/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.POST, "/admin/**").hasAnyRole("ADMIN", "EDITOR")
                        
                        // Support: check permissions (WebSocket endpoints will be checked in controller)
                        .requestMatchers(HttpMethod.GET, "/api/support/**").hasAuthority("SUPPORT_VIEW_PENDING")
                        .requestMatchers(HttpMethod.POST, "/api/support/**").hasAuthority("SUPPORT_VIEW_PENDING")

                        // Products: check permissions instead of role
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("PRODUCT_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("PRODUCT_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("PRODUCT_DELETE")

                        // Promotions: check permissions instead of role
                        .requestMatchers(HttpMethod.POST, "/api/promotions/**").hasAuthority("PROMOTION_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/promotions/**").hasAuthority("PROMOTION_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/promotions/**").hasAuthority("PROMOTION_DELETE")
                        
                        // Orders: check permissions
                        .requestMatchers(HttpMethod.GET, "/api/admin/orders/**").hasAuthority("ORDER_VIEW")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/orders/**").hasAuthority("ORDER_UPDATE")
                        
                        // Transactions: check permissions
                        .requestMatchers(HttpMethod.GET, "/api/admin/transactions/**").hasAnyAuthority("TRANSACTION_VIEW", "TRANSACTION_SUMMARY")
                        
                        // Permissions management: only ADMIN (keep role-based for security)
                        .requestMatchers("/api/admin/permissions/**").hasRole("ADMIN")
                        .requestMatchers("/api/notifications", "/api/notifications/**").permitAll()
                                               
                        // All other requests require authentication
                        .anyRequest().authenticated())
                                .formLogin(AbstractHttpConfigurer::disable)
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/login.html")
                                                .failureUrl("/login.html?error=true")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .exceptionHandling(exceptions -> exceptions
                                        .accessDeniedHandler(new AccessDeniedHandler() {
                                            @Override
                                            public void handle(HttpServletRequest request, HttpServletResponse response,
                                                    AccessDeniedException accessDeniedException) throws IOException {
                                                log.error("Access denied for request: {} {} - User: {}, Authorities: {}", 
                                                    request.getMethod(), 
                                                    request.getRequestURI(),
                                                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                                                    org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                        .getAuthentication() != null 
                                                        ? org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                            .getAuthentication().getAuthorities()
                                                        : "no authorities");
                                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                                response.setContentType("application/json");
                                                response.getWriter().write("{\"error\":\"Access denied\"}");
                                            }
                                        }));

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
