package com.futapp.config;

import com.futapp.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defensa adicional para la autenticacion basada en cookies. Los navegadores
 * envian Origin/Sec-Fetch-Site en solicitudes que pueden modificar datos, lo
 * que permite rechazar formularios disparados desde otro sitio sin afectar a
 * clientes nativos o pruebas que usan Bearer tokens.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class CrossSiteRequestFilter extends OncePerRequestFilter {
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private final Set<String> allowedOrigins;
    private final JsonMapper jsonMapper;

    public CrossSiteRequestFilter(
            @Value("${futapp.cors.allowed-origins:http://localhost:5173}") String origins,
            JsonMapper jsonMapper) {
        this.allowedOrigins = Arrays.stream(origins.split("\\s*,\\s*"))
                .filter(origin -> !origin.isBlank())
                .map(CrossSiteRequestFilter::normalizeOrigin)
                .collect(Collectors.toUnmodifiableSet());
        if (allowedOrigins.isEmpty()) throw new IllegalStateException("Debe configurarse al menos un origen permitido");
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SAFE_METHODS.contains(request.getMethod()) || isTrustedBrowserRequest(request)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), new ErrorResponse(403, "Origen de solicitud no permitido"));
    }

    private boolean isTrustedBrowserRequest(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            try {
                return allowedOrigins.contains(normalizeOrigin(origin));
            } catch (IllegalStateException exception) {
                return false;
            }
        }
        String fetchSite = request.getHeader("Sec-Fetch-Site");
        return fetchSite == null || !Set.of("cross-site", "none").contains(fetchSite.toLowerCase());
    }

    static String normalizeOrigin(String value) {
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            if (scheme == null || uri.getHost() == null || uri.getUserInfo() != null || uri.getPath() == null
                    || !uri.getPath().isEmpty() || uri.getQuery() != null || uri.getFragment() != null
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalStateException("Origen CORS invalido: " + value);
            }
            int port = uri.getPort();
            return scheme.toLowerCase() + "://" + uri.getHost().toLowerCase() + (port < 0 ? "" : ":" + port);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Origen CORS invalido: " + value, exception);
        }
    }
}
