package com.sg.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para coleta automática de métricas.
 * 
 * Registra automaticamente:
 * - Duração de cada requisição
 * - Status code
 * - Endpoint
 * - Método HTTP
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private final MetricsConfig metricsConfig;
    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();

    public MetricsInterceptor(MetricsConfig metricsConfig) {
        this.metricsConfig = metricsConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        Long start = startTime.get();
        if (start != null) {
            long duration = System.currentTimeMillis() - start;
            String method = request.getMethod();
            String endpoint = normalizeEndpoint(request.getRequestURI());
            int status = response.getStatus();

            // Registra métricas
            metricsConfig.recordRequest(method, endpoint, status, duration);

            // Registra erro se status >= 400
            if (status >= 400) {
                String errorType = getErrorType(status);
                metricsConfig.recordError(errorType, endpoint);
            }

            startTime.remove();
        }
    }

    /**
     * Normaliza o endpoint para evitar Cardinality Explosion
     * Ex: /api/usuarios/123 → /api/usuarios/{id}
     */
    private String normalizeEndpoint(String uri) {
        // Remove query string
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }

        // Normaliza paths com IDs
        String[] segments = uri.split("/");
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            
            if (i > 0) {
                normalized.append("/");
            }

            // Se parece um ID (número), substitui por {id}
            if (segment.matches("\\d+") || segment.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                normalized.append("{id}");
            } else {
                normalized.append(segment);
            }
        }

        return normalized.toString();
    }

    /**
     * Retorna o tipo de erro baseado no status code
     */
    private String getErrorType(int status) {
        if (status >= 500) {
            return "SERVER_ERROR";
        } else if (status == 404) {
            return "NOT_FOUND";
        } else if (status == 403) {
            return "FORBIDDEN";
        } else if (status == 401) {
            return "UNAUTHORIZED";
        } else if (status == 429) {
            return "RATE_LIMITED";
        } else if (status >= 400) {
            return "CLIENT_ERROR";
        }
        return "UNKNOWN";
    }
}
