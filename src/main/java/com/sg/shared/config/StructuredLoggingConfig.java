package com.sg.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Configuração de logs estruturados para o SGE.
 * 
 * Substitui System.out.println por logs estruturados em JSON.
 * Facilita:
 * - Análise de logs com ferramentas ELK/Datadog
 * - Rastreamento de requisições
 * - Diagnóstico de problemas em produção
 */
@Configuration
public class StructuredLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(StructuredLoggingConfig.class);

    /**
     * Logger estruturado para requisições HTTP
     */
    public static class StructuredLogger {
        
        private final Logger logger;
        private final String className;

        public StructuredLogger(Class<?> clazz) {
            this.logger = LoggerFactory.getLogger(clazz);
            this.className = clazz.getSimpleName();
        }

        /**
         * Log de requisição recebida
         */
        public void logRequestReceived(String method, String path, String clientIP) {
            Map<String, Object> logData = createBaseLogData("REQUEST_RECEIVED");
            logData.put("method", method);
            logData.put("path", path);
            logData.put("client_ip", clientIP);
            logData.put("timestamp", Instant.now().toString());
            
            logger.info("{}", logData);
        }

        /**
         * Log de requisição processada
         */
        public void logRequestProcessed(String method, String path, int statusCode, long durationMs) {
            Map<String, Object> logData = createBaseLogData("REQUEST_PROCESSED");
            logData.put("method", method);
            logData.put("path", path);
            logData.put("status_code", statusCode);
            logData.put("duration_ms", durationMs);
            logData.put("timestamp", Instant.now().toString());
            
            if (statusCode >= 400) {
                logger.warn("{}", logData);
            } else {
                logger.info("{}", logData);
            }
        }

        /**
         * Log de erro
         */
        public void logError(String message, Throwable throwable) {
            Map<String, Object> logData = createBaseLogData("ERROR");
            logData.put("message", message);
            logData.put("error_class", throwable.getClass().getSimpleName());
            logData.put("error_message", throwable.getMessage());
            logData.put("timestamp", Instant.now().toString());
            
            if (throwable.getStackTrace().length > 0) {
                StackTraceElement topFrame = throwable.getStackTrace()[0];
                logData.put("stack_trace", String.format("%s.%s(%s:%d)",
                    topFrame.getClassName(),
                    topFrame.getMethodName(),
                    topFrame.getFileName(),
                    topFrame.getLineNumber()));
            }
            
            logger.error("{}", logData, throwable);
        }

        /**
         * Log de segurança
         */
        public void logSecurityEvent(String event, String details) {
            Map<String, Object> logData = createBaseLogData("SECURITY_EVENT");
            logData.put("event", event);
            logData.put("details", details);
            logData.put("timestamp", Instant.now().toString());
            
            // Adiciona informações da requisição se disponível
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logData.put("client_ip", getClientIP(request));
                logData.put("user_agent", request.getHeader("User-Agent"));
                logData.put("path", request.getRequestURI());
            }
            
            logger.warn("{}", logData);
        }

        /**
         * Log de performance
         */
        public void logPerformance(String operation, long durationMs, boolean success) {
            Map<String, Object> logData = createBaseLogData("PERFORMANCE");
            logData.put("operation", operation);
            logData.put("duration_ms", durationMs);
            logData.put("success", success);
            logData.put("timestamp", Instant.now().toString());
            
            if (durationMs > 1000) { // Operações lentas
                logger.warn("{}", logData);
            } else {
                logger.debug("{}", logData);
            }
        }

        /**
         * Log de auditoria
         */
        public void logAudit(String action, String entity, Long entityId, String details) {
            Map<String, Object> logData = createBaseLogData("AUDIT");
            logData.put("action", action);
            logData.put("entity", entity);
            logData.put("entity_id", entityId);
            logData.put("details", details);
            logData.put("timestamp", Instant.now().toString());
            
            // Adiciona usuário se disponível
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logData.put("client_ip", getClientIP(request));
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    logData.put("has_auth", true);
                }
            }
            
            logger.info("{}", logData);
        }

        private Map<String, Object> createBaseLogData(String level) {
            Map<String, Object> logData = new LinkedHashMap<>();
            logData.put("level", level);
            logData.put("logger", className);
            logData.put("thread", Thread.currentThread().getName());
            
            // Adiciona correlation ID se disponível
            String correlationId = getCorrelationId();
            if (correlationId != null) {
                logData.put("correlation_id", correlationId);
            }
            
            return logData;
        }

        private String getCorrelationId() {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId != null) {
                    return correlationId;
                }
                // Gera um novo correlation ID se não existir
                return UUID.randomUUID().toString();
            }
            return null;
        }

        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
    }

    /**
     * Factory para criar loggers estruturados
     */
    public static StructuredLogger getLogger(Class<?> clazz) {
        return new StructuredLogger(clazz);
    }
}
