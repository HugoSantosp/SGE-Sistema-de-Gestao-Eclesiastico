package com.sg.shared.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator personalizado para verificar a saúde do banco de dados.
 * 
 * Verifica:
 * - Conexão com o banco
 * - Tempo de resposta
 * - Versão do banco
 * - Espaço em disco disponível
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);
    
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Testa conexão
            try (Connection connection = dataSource.getConnection()) {
                long connectionTime = System.currentTimeMillis() - startTime;
                
                // Executa query de teste
                long queryStart = System.currentTimeMillis();
                String dbVersion = jdbcTemplate.queryForObject(
                    "SELECT version()", String.class);
                long queryTime = System.currentTimeMillis() - queryStart;
                
                // Verifica espaço em disco (para PostgreSQL)
                Map<String, Object> details = new HashMap<>();
                details.put("database", connection.getMetaData().getDatabaseProductName());
                details.put("version", dbVersion);
                details.put("connectionTimeMs", connectionTime);
                details.put("queryTimeMs", queryTime);
                details.put("url", connection.getMetaData().getURL());
                details.put("username", connection.getMetaData().getUserName());
                details.put("timestamp", Instant.now().toString());
                
                // Tenta obter tamanho do banco (PostgreSQL)
                try {
                    Long dbSize = jdbcTemplate.queryForObject(
                        "SELECT pg_database_size(current_database())", Long.class);
                    details.put("databaseSizeBytes", dbSize);
                    details.put("databaseSizeMB", dbSize / (1024 * 1024));
                } catch (Exception e) {
                    // Ignora se não for PostgreSQL
                }
                
                // Tenta obter número de conexões ativas (PostgreSQL)
                try {
                    Integer activeConnections = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM pg_stat_activity WHERE datname = current_database()", 
                        Integer.class);
                    details.put("activeConnections", activeConnections);
                } catch (Exception e) {
                    // Ignora se não for PostgreSQL
                }
                
                if (connectionTime > 1000) {
                    // Conexão lenta
                    return Health.down()
                        .withDetails(details)
                        .withDetail("warning", "Database connection is slow")
                        .build();
                }
                
                return Health.up()
                    .withDetails(details)
                    .build();
            }
            
        } catch (SQLException e) {
            logger.error("Database health check failed", e);
            
            Map<String, Object> details = new HashMap<>();
            details.put("error", e.getMessage());
            details.put("sqlState", e.getSQLState());
            details.put("errorCode", e.getErrorCode());
            details.put("timestamp", Instant.now().toString());
            
            return Health.down()
                .withDetails(details)
                .withException(e)
                .build();
        }
    }
}
