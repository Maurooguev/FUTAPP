package com.futapp.config;

import com.futapp.model.Usuario;
import com.futapp.service.AuditoriaAdministrativaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuditoriaInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaInterceptor.class);
    private static final Set<String> LECTURA = Set.of("GET", "HEAD", "OPTIONS");
    private final AuditoriaAdministrativaService auditoria;

    public AuditoriaInterceptor(AuditoriaAdministrativaService auditoria) { this.auditoria = auditoria; }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (LECTURA.contains(request.getMethod())) return;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = auth != null && auth.getPrincipal() instanceof Usuario item ? item : null;
        String requestId = String.valueOf(request.getAttribute(RequestTraceFilter.REQUEST_ID_ATTRIBUTE));
        try {
            auditoria.registrar(usuario, request.getMethod(), request.getRequestURI(), response.getStatus(), requestId);
        } catch (Exception error) {
            log.error("No se pudo guardar la auditoría requestId={}", requestId, error);
        }
    }
}
