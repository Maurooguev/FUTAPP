package com.futapp.config;

import com.futapp.repository.AuthTokenRepository;
import com.futapp.security.TokenSecurity;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

public class BearerTokenFilter extends OncePerRequestFilter {
    private final AuthTokenRepository tokens;
    public BearerTokenFilter(AuthTokenRepository tokens) { this.tokens = tokens; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String rawToken = resolveToken(request);
        if (rawToken != null && !rawToken.isBlank()) {
            var storedToken = tokens.findById(TokenSecurity.hash(rawToken));
            storedToken.filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now())).ifPresent(token -> {
                var user = token.getUsuario();
                var authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
            });
            storedToken.filter(token -> !token.getExpiresAt().isAfter(LocalDateTime.now()))
                    .ifPresent(token -> tokens.deleteById(token.getToken()));
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.futapp.model.Usuario user
                && user.isPasswordChangeRequired() && mustChangePasswordBefore(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":403,\"message\":\"Debés cambiar la contraseña temporal antes de continuar\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean mustChangePasswordBefore(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.contains("/auth/")) return false;
        if (!"GET".equalsIgnoreCase(request.getMethod())) return true;
        return path.contains("/torneos/mios") || path.contains("/perfiles-organizador/mio")
                || path.contains("/admin/") || path.contains("/auditoria/");
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            String cookieToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "FUTAPP_SESSION".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .filter(value -> !value.isBlank())
                    .findFirst().orElse(null);
            if (cookieToken != null) return cookieToken;
        }
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7).trim() : null;
    }
}
