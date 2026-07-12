package com.tandiantong.bootstrap.security;

import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.auth.TokenService;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final DatabaseAuthenticationService databaseAuthenticationService;

    public JwtAuthenticationFilter(TokenService tokenService, DatabaseAuthenticationService databaseAuthenticationService) {
        this.tokenService = tokenService;
        this.databaseAuthenticationService = databaseAuthenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                CurrentUser user = databaseAuthenticationService.resolve(tokenService.parse(authorization.substring(7)));
                SecurityContextHolder.set(user);
                SecurityContext securityContext = org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(user, null,
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.domain().name()))));
                org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clear();
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }
}
