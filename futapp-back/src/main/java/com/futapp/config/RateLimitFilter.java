package com.futapp.config;

import com.futapp.exception.ErrorResponse;
import com.futapp.security.TokenSecurity;
import com.futapp.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Duration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {
    private record Policy(String name, int limit, Duration window, String message) {}
    private final RateLimitService limits;
    private final JsonMapper jsonMapper;

    public RateLimitFilter(RateLimitService limits, JsonMapper jsonMapper) {
        this.limits = limits;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Policy policy = policyFor(request);
        if (policy == null) {
            chain.doFilter(request, response);
            return;
        }
        String client = clientKey(request);
        RateLimitService.Decision decision = limits.consume(policy.name() + ":" + client, policy.limit(), policy.window());
        response.setHeader("X-RateLimit-Limit", Integer.toString(policy.limit()));
        response.setHeader("X-RateLimit-Remaining", Integer.toString(decision.remaining()));
        if (!decision.allowed()) {
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            jsonMapper.writeValue(response.getOutputStream(), new ErrorResponse(429, policy.message()));
            return;
        }
        chain.doFilter(request, response);
    }

    private Policy policyFor(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equals(method) && path.endsWith("/auth/login"))
            return new Policy("login", 30, Duration.ofMinutes(5), "Demasiados intentos de ingreso. Esperá unos minutos");
        if ("POST".equals(method) && path.endsWith("/auth/register"))
            return new Policy("register", 10, Duration.ofHours(1), "Se crearon demasiadas cuentas desde esta conexión. Intentá más tarde");
        if ("POST".equals(method) && path.endsWith("/auth/password/forgot"))
            return new Policy("forgot", 5, Duration.ofMinutes(15), "Solicitaste demasiados enlaces de recuperación. Esperá 15 minutos");
        if ("POST".equals(method) && path.endsWith("/auth/password/reset"))
            return new Policy("reset", 10, Duration.ofMinutes(15), "Demasiados intentos de recuperación. Esperá 15 minutos");
        if ("GET".equals(method) && path.endsWith("/busqueda"))
            return new Policy("search", 120, Duration.ofMinutes(1), "Realizaste demasiadas búsquedas. Esperá un momento");
        if (path.contains("/reclamos/") && "POST".equals(method))
            return new Policy("claim-send", 20, Duration.ofMinutes(10), "Enviaste demasiadas solicitudes. Esperá unos minutos");
        if (path.contains("/reclamos/") && "PUT".equals(method))
            return new Policy("claim-review", 60, Duration.ofMinutes(10), "Procesaste demasiadas solicitudes. Esperá unos minutos");
        return null;
    }

    private String clientKey(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return "token-" + TokenSecurity.hash(authorization.substring(7).trim());
        }
        String address = request.getRemoteAddr();
        return "ip-" + (address == null || address.isBlank() ? "unknown" : address);
    }
}
