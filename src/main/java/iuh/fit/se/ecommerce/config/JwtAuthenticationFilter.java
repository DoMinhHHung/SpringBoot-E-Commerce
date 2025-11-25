package iuh.fit.se.ecommerce.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final iuh.fit.se.ecommerce.service.impl.CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String header = request.getHeader("Authorization");
            log.info("Incoming request {} {} - Authorization header present: {}", request.getMethod(), request.getRequestURI(), header != null);

            String token = null;
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            } else {
                // Try reading from cookie named access_token
                if (request.getCookies() != null) {
                    for (var c : request.getCookies()) {
                        if ("access_token".equals(c.getName())) {
                            token = c.getValue();
                            break;
                        }
                    }
                }
            }

            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            log.info("Received JWT token (len={})", token.length());

            boolean isValidToken = jwtTokenProvider.validateToken(token);
            boolean hasExistingAuth = SecurityContextHolder.getContext().getAuthentication() != null;
            
            log.info("JWT token validation: valid={}, hasExistingAuth={}", isValidToken, hasExistingAuth);

            if (isValidToken && !hasExistingAuth) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                log.info("JWT token valid, loading user details for email: {}", email);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                
                log.info("User details loaded - username={}, authorities count={}, authorities={}", 
                    userDetails.getUsername(), 
                    userDetails.getAuthorities().size(),
                    userDetails.getAuthorities());

                // If account is locked (banned), reject with 403
                try {
                    java.lang.reflect.Method m = userDetails.getClass().getMethod("isAccountNonLocked");
                    boolean nonLocked = (boolean) m.invoke(userDetails);
                    if (!nonLocked) {
                        log.warn("Authentication denied for banned user: {}", email);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"message\": \"Tài khoản của bạn đã bị chặn\"}");
                        return;
                    }
                } catch (NoSuchMethodException nsme) {
                    // fallback: continue
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JWT authenticated successfully for user: {} with authorities: {}", 
                    email, userDetails.getAuthorities());
            } else {
                if (!isValidToken) {
                    log.warn("JWT token validation failed for request: {}", request.getRequestURI());
                }
                if (hasExistingAuth) {
                    log.debug("User already authenticated, skipping JWT processing");
                }
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
