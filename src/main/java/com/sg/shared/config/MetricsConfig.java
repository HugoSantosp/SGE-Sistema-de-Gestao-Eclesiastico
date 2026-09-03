package com.sg.shared.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de métricas personalizadas para o SGE.
 * 
 * Métricas disponíveis no Prometheus:
 * - sge_requests_total: Total de requisições por endpoint
 * - sge_request_duration_seconds: Duração das requisições
 * - sge_active_users: Usuários ativos
 * - sge_error_total: Total de erros por tipo
 * - sge_login_attempts_total: Tentativas de login
 */
@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Contador de requisições totais
     */
    @Bean
    public Counter requestCounter() {
        return Counter.builder("sge.requests.total")
            .description("Total de requisições HTTP")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Contador de requisições por endpoint
     */
    @Bean
    public Counter requestCounterByEndpoint() {
        return Counter.builder("sge.requests.by.endpoint")
            .description("Requisições por endpoint")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Timer de duração de requisições
     */
    @Bean
    public Timer requestTimer() {
        return Timer.builder("sge.request.duration")
            .description("Duração das requisições HTTP")
            .tag("application", "sge-backend")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(meterRegistry);
    }

    /**
     * Contador de tentativas de login
     */
    @Bean
    public Counter loginAttemptsCounter() {
        return Counter.builder("sge.login.attempts.total")
            .description("Total de tentativas de login")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Contador de logins bem-sucedidos
     */
    @Bean
    public Counter loginSuccessCounter() {
        return Counter.builder("sge.login.success.total")
            .description("Total de logins bem-sucedidos")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Contador de falhas de login
     */
    @Bean
    public Counter loginFailureCounter() {
        return Counter.builder("sge.login.failure.total")
            .description("Total de falhas de login")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Contador de erros por tipo
     */
    @Bean
    public Counter errorCounter() {
        return Counter.builder("sge.errors.total")
            .description("Total de erros por tipo")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Contador de operações CRUD
     */
    @Bean
    public Counter crudOperationsCounter() {
        return Counter.builder("sge.crud.operations.total")
            .description("Operações CRUD realizadas")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Summary de tamanho de upload
     */
    @Bean
    public DistributionSummary uploadSizeSummary() {
        return DistributionSummary.builder("sge.upload.size.bytes")
            .description("Tamanho dos uploads em bytes")
            .tag("application", "sge-backend")
            .baseUnit("bytes")
            .register(meterRegistry);
    }

    /**
     * Timer de operações de banco de dados
     */
    @Bean
    public Timer dbOperationTimer() {
        return Timer.builder("sge.db.operation.duration")
            .description("Duração das operações de banco de dados")
            .tag("application", "sge-backend")
            .publishPercentiles(0.5, 0.75, 0.95, 0.99)
            .register(meterRegistry);
    }

    /**
     * Contador de usuários ativos
     */
    @Bean
    public Counter activeUsersCounter() {
        return Counter.builder("sge.active.users")
            .description("Usuários ativos no sistema")
            .tag("application", "sge-backend")
            .register(meterRegistry);
    }

    /**
     * Métodos utilitários para registrar métricas
     */
    public void recordRequest(String method, String endpoint, int status, long durationMs) {
        requestCounter().increment();
        requestTimer().record(durationMs, TimeUnit.MILLISECONDS);
        
        meterRegistry.counter("sge.requests.by.endpoint",
            "method", method,
            "endpoint", endpoint,
            "status", String.valueOf(status)
        ).increment();
    }

    public void recordLoginAttempt(boolean success) {
        loginAttemptsCounter().increment();
        if (success) {
            loginSuccessCounter().increment();
        } else {
            loginFailureCounter().increment();
        }
    }

    public void recordError(String errorType, String endpoint) {
        meterRegistry.counter("sge.errors.total",
            "type", errorType,
            "endpoint", endpoint
        ).increment();
    }

    public void recordCrudOperation(String operation, String entity) {
        meterRegistry.counter("sge.crud.operations.total",
            "operation", operation,
            "entity", entity
        ).increment();
    }

    public void recordUploadSize(long sizeBytes) {
        uploadSizeSummary().record(sizeBytes);
    }

    public void recordDbOperation(String operation, long durationMs) {
        dbOperationTimer().record(durationMs, TimeUnit.MILLISECONDS);
    }
}
