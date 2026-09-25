package com.futapp.config;

import com.futapp.exception.ErrorResponse;
import com.futapp.repository.AuthTokenRepository;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean UserDetailsService userDetailsService() { return username -> { throw new UsernameNotFoundException(username); }; }
    @Bean SecurityFilterChain filterChain(HttpSecurity http, AuthTokenRepository tokens,
                                          JsonMapper jsonMapper) throws Exception {
        return http.csrf(csrf -> csrf.disable()).cors(cors -> {}).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login", "/auth/register", "/auth/password/forgot", "/auth/password/reset", "/status", "/hola").permitAll()
                .requestMatchers("/auth/me", "/auth/logout", "/auth/password", "/torneos/mios", "/torneos/mios/**", "/equipos/mios").authenticated()
                .requestMatchers("/perfiles-organizador/mio").authenticated()
                .requestMatchers(HttpMethod.POST, "/torneos").hasAnyRole("ORGANIZADOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/torneos/*/visitas", "/jugadores/*/visitas").permitAll()
                .requestMatchers(HttpMethod.PUT, "/torneos/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/torneos/**").authenticated()
                .requestMatchers("/reclamos/**", "/perfil-jugador/**").authenticated()
                .requestMatchers("/notificaciones/**").authenticated()
                .requestMatchers("/auditoria/**").hasRole("ADMIN")
                .requestMatchers("/favoritos/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/archivos/imagenes").authenticated()
                .requestMatchers(HttpMethod.GET, "/equipos/*/permisos").authenticated()
                .requestMatchers(HttpMethod.PUT, "/equipos/*", "/jugadores/*").authenticated()
                .requestMatchers(HttpMethod.POST, "/jugadores", "/inscripciones/*/jugadores").authenticated()
                .requestMatchers(HttpMethod.POST, "/equipos", "/inscripciones/equipos").authenticated()
                .requestMatchers(HttpMethod.GET, "/equipos/creados").authenticated()
                .requestMatchers(HttpMethod.PUT, "/inscripciones/jugadores/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/jugadores/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                .anyRequest().hasAnyRole("ORGANIZADOR", "ADMIN"))
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    jsonMapper.writeValue(response.getOutputStream(),
                            new ErrorResponse(401, "Autenticación requerida"));
                })
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    String message = "POST".equals(request.getMethod()) && request.getRequestURI().endsWith("/torneos")
                            ? "Debés crear una cuenta de organizador de torneos para realizar esta acción"
                            : "No tenés permisos para realizar esta acción";
                    jsonMapper.writeValue(response.getOutputStream(),
                            new ErrorResponse(403, message));
                }))
            .addFilterBefore(new BearerTokenFilter(tokens), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
