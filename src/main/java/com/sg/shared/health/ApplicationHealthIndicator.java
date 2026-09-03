package com.sg.shared.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health indicator para verificar a saúde da aplicação.
 * 
 * Verifica:
 * - Memória utilizada
 * - Uptime da aplicação
 * - Thread count
 * - CPU usage
 */
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationHealthIndicator.class);
    
    private final long startTime = System.currentTimeMillis();

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // Informações de memória
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
            
            details.put("heapMemory", Map.of(
                "used", formatBytes(heapMemory.getUsed()),
                "committed", formatBytes(heapMemory.getCommitted()),
                "max", formatBytes(heapMemory.getMax()),
                "usagePercent", calculateUsagePercent(heapMemory)
            ));
            
            details.put("nonHeapMemory", Map.of(
                "used", formatBytes(nonHeapMemory.getUsed()),
                "committed", formatBytes(nonHeapMemory.getCommitted())
            ));
            
            // Informações de runtime
            RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
            details.put("uptimeMs", runtimeBean.getUptime());
            details.put("uptimeFormatted", formatUptime(runtimeBean.getUptime()));
            details.put("startTime", Instant.ofEpochMilli(runtimeBean.getStartTime()).toString());
            
            // Thread count
            details.put("threadCount", Thread.activeCount());
            details.put("peakThreadCount", ManagementFactory.getThreadMXBean().getPeakThreadCount());
            
            // CPU
            details.put("availableProcessors", Runtime.getRuntime().availableProcessors());
            
            // Timestamp
            details.put("timestamp", Instant.now().toString());
            details.put("application", "sge-backend");
            details.put("version", "1.0.0");
            
            // Verifica se a memória está muito alta
            double usagePercent = calculateUsagePercent(heapMemory);
            if (usagePercent > 90) {
                return Health.down()
                    .withDetails(details)
                    .withDetail("warning", "High memory usage: " + usagePercent + "%")
                    .build();
            }
            
            return Health.up()
                .withDetails(details)
                .build();
            
        } catch (Exception e) {
            logger.error("Application health check failed", e);
            return Health.down()
                .withException(e)
                .build();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private double calculateUsagePercent(MemoryUsage memory) {
        if (memory.getMax() == -1) return 0;
        return (double) memory.getUsed() / memory.getMax() * 100;
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d days, %d hours, %d minutes", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds % 60);
        } else {
            return String.format("%d seconds", seconds);
        }
    }
}
