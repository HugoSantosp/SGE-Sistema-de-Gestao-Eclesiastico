package com.sg.shared.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Configuração de Rate Limiting para endpoints públicos.
 * 
 * Protege contra:
 * - Força bruta no login
 * - Scraping de dados públicos
 * - Abuso de APIs públicas
 * 
 * Limites padrão:
 * - Login: 5 tentativas por minuto
 * - APIs públicas: 60 requisições por minuto
 * - APIs gerais: 120 requisições por minuto
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    /**
     * Mapa de buckets por IP
     */
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Configuração de limites por tipo de endpoint
     */
    public enum RateLimitType {
        LOGIN(5, Duration.ofMinutes(1)),           // 5 tentativas/minuto
        PUBLIC_API(60, Duration.ofMinutes(1)),     // 60 req/minuto
        GENERAL_API(120, Duration.ofMinutes(1)),   // 120 req/minuto
        ADMIN_API(300, Duration.ofMinutes(1));     // 300 req/minuto (admin)

        private final int limit;
        private final Duration period;

        RateLimitType(int limit, Duration period) {
            this.limit = limit;
            this.period = period;
        }

        public int getLimit() { return limit; }
        public Duration getPeriod() { return period; }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor())
                .addPathPatterns("/auth/**", "/api/public/**", "/api/publico/**")
                .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**");
    }

    /**
     * Interceptor de Rate Limiting
     */
    public class RateLimitInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String clientIP = getClientIP(request);
            String path = request.getRequestURI();

            // Determina o tipo de rate limit baseado no path
            RateLimitType limitType = determineLimitType(path);

            // Obtém ou cria bucket para este IP
            Bucket bucket = buckets.computeIfAbsent(clientIP + ":" + limitType.name(),
                    k -> createBucket(limitType));

            // Tenta consumir uma requisição
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                // Adiciona headers de rate limit
                response.addHeader("X-Rate-Limit-Remaining", 
                    String.valueOf(probe.getRemainingTokens()));
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", 
                    String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
                return true;
            } else {
                // Limite excedido
                long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", 
                    String.valueOf(waitTimeSeconds));
                
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                
                try {
                    response.getWriter().write(String.format(
                        "{\"status\":429,\"error\":\"Muitas requisições\",\"message\":\"Limite de requisições excedido. Tente novamente em %d segundos.\",\"retryAfter\":%d}",
                        waitTimeSeconds, waitTimeSeconds
                    ));
                } catch (Exception e) {
                    // Ignora erro de escrita
                }
                
                return false;
            }
        }

        private Bucket createBucket(RateLimitType limitType) {
            Bandwidth limit = Bandwidth.classic(limitType.getLimit(), 
                Refill.greedy(limitType.getLimit(), limitType.getPeriod()));
            return Bucket.builder().addLimit(limit).build();
        }

        private RateLimitType determineLimitType(String path) {
            if (path.startsWith("/auth/login")) {
                return RateLimitType.LOGIN;
            } else if (path.startsWith("/api/public") || path.startsWith("/api/publico")) {
                return RateLimitType.PUBLIC_API;
            } else if (path.startsWith("/api/admin")) {
                return RateLimitType.ADMIN_API;
            }
            return RateLimitType.GENERAL_API;
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

    /**
     * Limpa buckets antigos periodicamente (opcional - para memória)
     */
    public void cleanupOldBuckets() {
        // Implementar limpeza periódica se necessário
        // Por exemplo, remover buckets que não foram usados nas últimas 2 horas
    }
}
