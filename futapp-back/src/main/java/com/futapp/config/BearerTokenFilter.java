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

public class BearerTokenFilter extends OncePerRequestFilter {
    private final AuthTokenRepository tokens;
    public BearerTokenFilter(AuthTokenRepository tokens) { this.tokens = tokens; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String rawToken = header.substring(7).trim();
            var storedToken = tokens.findById(TokenSecurity.hash(rawToken));
            if (storedToken.isEmpty()) storedToken = tokens.findById(rawToken); // Sesiones anteriores al hash.
            storedToken.filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now())).ifPresent(token -> {
                var user = token.getUsuario();
                var authorities = user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));
            });
        }
        chain.doFilter(request, response);
    }
}
