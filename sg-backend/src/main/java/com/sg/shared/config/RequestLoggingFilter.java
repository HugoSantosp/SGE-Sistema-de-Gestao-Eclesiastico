package com.sg.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para logging de requisições HTTP.
 * 
 * Funcionalidades:
 * - Gera correlation ID para cada requisição
 * - Registra método, path, status e duração
 * - Loga headers importantes (User-Agent, X-Forwarded-For)
 * - Não loga dados sensíveis (senha, token)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private final StructuredLoggingConfig.StructuredLogger structuredLogger = 
        StructuredLoggingConfig.getLogger(RequestLoggingFilter.class);

    // Headers que não devem ser logados (dados sensíveis)
    private static final java.util.Set<String> SENSITIVE_HEADERS = java.util.Set.of(
        "authorization", "cookie", "x-api-key", "x-secret"
    );

    // Paths que não devem ser logados (muita frequência)
    private static final java.util.Set<String> EXCLUDED_PATHS = java.util.Set.of(
        "/actuator/health", "/actuator/prometheus", "/favicon.ico"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        String path = request.getRequestURI();
        
        // Pula logging para paths excluídos
        if (EXCLUDED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        long startTime = System.currentTimeMillis();
        String clientIP = getClientIP(request);
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        
        // Log de requisição recebida
        structuredLogger.logRequestReceived(method, path, clientIP);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = response.getStatus();
            
            // Log de requisição processada
            structuredLogger.logRequestProcessed(method, path, statusCode, duration);
            
            // Log de performance para requisições lentas
            if (duration > 1000) {
                structuredLogger.logPerformance("HTTP " + method + " " + path, duration, statusCode < 400);
            }
            
            // Adiciona correlation ID na resposta
            response.setHeader("X-Correlation-ID", correlationId);
            response.setHeader("X-Response-Time", String.valueOf(duration));
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
