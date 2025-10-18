package softtech.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import softtech.server.models.Account;
import softtech.server.services.AuthService;
import softtech.server.utils.JwtUtil;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final AuthService userService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, AuthService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // Kiểm tra chữ ký + expiration
                jwtUtil.verifyToken(token);
                logger.debug("Token signature + expiration OK");

                // Trích payload
                String accountId = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                logger.debug("Token payload: accountId={}, username={}, role={}", accountId, username, role);

                Account user = userService.findById(accountId);
                if (user == null) {
                    logger.warn("User not found in DB for accountId {}", accountId);
                } else {
                    boolean usernameMatch = username != null &&
                            username.trim().equalsIgnoreCase(user.getUsername().trim());
                    boolean roleMatch = role != null &&
                            role.trim().equalsIgnoreCase(user.getRole().toString().trim());

                    if (usernameMatch && roleMatch &&
                            SecurityContextHolder.getContext().getAuthentication() == null) {

                        List<SimpleGrantedAuthority> authorities =
                                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString().trim().toUpperCase()));
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username.trim(), null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Authentication set for user: {}", username.trim());
                    } else {
                        logger.warn("Token validation failed: usernameMatch={} roleMatch={}", usernameMatch, roleMatch);
                    }
                }

            } catch (RuntimeException e) {
                logger.warn("Invalid JWT token: {}", e.getMessage());
            }

        } else {
            logger.debug("No Bearer token found in request header.");
        }

        filterChain.doFilter(request, response);
    }
}
