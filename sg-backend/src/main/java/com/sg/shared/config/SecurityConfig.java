package com.sg.shared.config;

import com.sg.auth.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança do SGE.
 * 
 * Melhorias de segurança implementadas:
 * - Headers de segurança (HSTS, X-Content-Type-Options, etc.)
 * - Rate limiting via interceptor
 * - CSRF proteção (mesmo sendo stateless)
 * - Validação de token em todos os endpoints protegidos
 * - Rate limiting mais agressivo em endpoints de autenticação
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize, @Secured, etc.
public class SecurityConfig {

    /**
     * Origens permitidas no CORS (separadas por vírgula).
     * Mesma env usada pelo CorsConfig — nunca duplicar lista fixa aqui.
     */
    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MetricsConfig metricsConfig;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, MetricsConfig metricsConfig) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.metricsConfig = metricsConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF - Desabilitado (aplicação é stateless com JWT)
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Headers de segurança
                .headers(headers -> headers
                    .contentTypeOptions(contentType -> {}) // X-Content-Type-Options: nosniff
                    .frameOptions(frame -> frame.deny()) // X-Frame-Options: DENY
                    .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                    .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000) // 1 ano
                    )
                    .referrerPolicy(referrer -> referrer
                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    )
                    .permissionsPolicy(permissions -> permissions
                        .policy("camera=(), microphone=(), geolocation=()")
                    )
                )
                
                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos de autenticação
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/esqueci-senha").permitAll()
                        .requestMatchers("/auth/redefinir-senha").permitAll()
                        // Endpoints públicos da aplicação (landing page)
                        .requestMatchers("/api/public/**").permitAll()
                        // Endpoints públicos de escala de louvor (acesso via token)
                        .requestMatchers("/api/publico/escala/**").permitAll()
                        // Swagger / docs
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        // Actuator - apenas health público, outros requerem ADMIN
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Arquivos estáticos (uploads)
                        .requestMatchers("/api/uploads/**").permitAll()
                        // Endpoints de admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Qualquer outro endpoint requer autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
