# 🔒 Melhorias de Segurança e Robustez - SGE Backend

Documentação completa das melhorias de segurança e robustez implementadas no backend do SGE.

---

## 📋 Resumo das Melhorias

| Melhoria | Status | Descrição |
|----------|--------|-----------|
| ✅ Rate Limiting | Implementado | Proteção contra força bruta e abuso |
| ✅ Logs Estruturados | Implementado | Logs em JSON para análise |
| ✅ Health Checks | Implementado | /actuator/health com detalhes |
| ✅ Métricas | Implementado | Micrometer + Prometheus |
| ✅ Headers de Segurança | Implementado | HSTS, X-Content-Type-Options, etc. |
| ✅ CORS Configurado | Implementado | Origens permitidas explícitas |

---

## 🛡️ Rate Limiting

### Configuração

O rate limiting é implementado usando **Bucket4j** com os seguintes limites:

| Endpoint | Limite | Período |
|----------|--------|---------|
| Login | 5 tentativas | 1 minuto |
| APIs Públicas | 60 requisições | 1 minuto |
| APIs Gerais | 120 requisições | 1 minuto |
| APIs Admin | 300 requisições | 1 minuto |

### Headers de Resposta

```
X-Rate-Limit-Remaining: 45
X-Rate-Limit-Retry-After-Seconds: 30
```

### Resposta quando limite excedido

```json
{
  "status": 429,
  "error": "Muitas requisições",
  "message": "Limite de requisições excedido. Tente novamente em 30 segundos.",
  "retryAfter": 30
}
```

### Como funciona

1. Cada IP tem seu próprio "bucket" de requisições
2. A cada requisição, 1 token é consumido
3. Tokens são repostos automaticamente
4. Quando tokens acabam, requisição é bloqueada

---

## 📝 Logs Estruturados

### Configuração

O logging usa **Logback** com formatos estruturados (JSON).

### Arquivos de Log

| Arquivo | Uso | Retenção |
|---------|-----|----------|
| `sge-backend.log` | Logs gerais | 30 dias |
| `sge-backend.json` | Logs estruturados (JSON) | 30 dias |
| `sge-audit.log` | Auditoria (CRUD) | 90 dias |
| `sge-security.log` | Segurança (login, auth) | 90 dias |

### Exemplo de Log JSON

```json
{
  "timestamp": "2026-08-26T10:30:15.123Z",
  "level": "INFO",
  "logger": "StructuredLogger",
  "thread": "http-nio-8080-exec-1",
  "message": {
    "level": "REQUEST_PROCESSED",
    "method": "POST",
    "path": "/auth/login",
    "status_code": 200,
    "duration_ms": 145,
    "client_ip": "192.168.1.100",
    "correlation_id": "abc-123-def-456"
  }
}
```

### Tipos de Log

#### 1. Log de Requisição
```java
structuredLogger.logRequestReceived("GET", "/api/usuarios", "192.168.1.1");
structuredLogger.logRequestProcessed("GET", "/api/usuarios", 200, 50);
```

#### 2. Log de Segurança
```java
structuredLogger.logSecurityEvent("LOGIN_FAILED", "Email não encontrado");
structuredLogger.logSecurityEvent("TOKEN_INVALID", "Token expirado");
```

#### 3. Log de Auditoria
```java
structuredLogger.logAudit("CREATE", "Membro", 123L, "Membro criado com sucesso");
structuredLogger.logAudit("DELETE", "Evento", 456L, "Evento removido");
```

#### 4. Log de Performance
```java
structuredLogger.logPerformance("SELECT * FROM membros", 150, true);
structuredLogger.logPerformance("INSERT INTO eventos", 2500, false);
```

---

## 🏥 Health Checks

### Endpoints Disponíveis

#### `/actuator/health` (Público)
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "version": "PostgreSQL 15.17",
        "connectionTimeMs": 5,
        "queryTimeMs": 12,
        "databaseSizeMB": 45,
        "activeConnections": 8
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": "500 GB",
        "free": "250 GB",
        "threshold": "10 GB"
      }
    },
    "sgeApplication": {
      "status": "UP",
      "details": {
        "heapMemory": {
          "used": "256 MB",
          "committed": "512 MB",
          "max": "1024 MB",
          "usagePercent": 25.0
        },
        "uptimeFormatted": "5 days, 12 hours, 30 minutes",
        "threadCount": 45
      }
    }
  }
}
```

#### `/actuator/info` (Público)
```json
{
  "app": {
    "name": "SGE Backend",
    "version": "1.0.0",
    "description": "Sistema de Gerenciamento Eclesiástico"
  }
}
```

#### `/actuator/prometheus` (Protegido - ADMIN)
Métricas no formato Prometheus para monitoramento.

### Health Indicators Implementados

1. **DatabaseHealthIndicator** - Verifica conexão com PostgreSQL
2. **ApplicationHealthIndicator** - Verifica memória, threads, uptime
3. **DiskSpaceHealthIndicator** - Verifica espaço em disco (padrão Spring)
4. **DbHealthIndicator** - Verifica pools de conexão (padrão Spring)

---

## 📊 Métricas (Micrometer + Prometheus)

### Métricas Disponíveis

#### Métricas de Requisições
```
sge_requests_total - Total de requisições
sge_request_duration_seconds - Duração das requisições
sge_requests_by_endpoint - Requisições por endpoint
```

#### Métricas de Login
```
sge_login_attempts_total - Total de tentativas de login
sge_login_success_total - Logins bem-sucedidos
sge_login_failure_total - Falhas de login
```

#### Métricas de Erros
```
sge_errors_total - Total de erros
sge_crud_operations_total - Operações CRUD
```

#### Métricas de Performance
```
sge_db_operation_duration_seconds - Duração de queries
sge_upload_size_bytes - Tamanho de uploads
```

### Exemplo de Métricas no Prometheus

```
# HELP sge_requests_total Total de requisições HTTP
# TYPE sge_requests_total counter
sge_requests_total{application="sge-backend"} 1547

# HELP sge_request_duration_seconds Duração das requisições HTTP
# TYPE sge_request_duration_seconds histogram
sge_request_duration_seconds_bucket{application="sge-backend",le="0.1"} 1200
sge_request_duration_seconds_bucket{application="sge-backend",le="0.5"} 1450
sge_request_duration_seconds_bucket{application="sge-backend",le="1.0"} 1520
sge_request_duration_seconds_bucket{application="sge-backend",le="+Inf"} 1547

# HELP sge_login_attempts_total Total de tentativas de login
# TYPE sge_login_attempts_total counter
sge_login_attempts_total{application="sge-backend"} 320
sge_login_success_total{application="sge-backend"} 310
sge_login_failure_total{application="sge-backend"} 10
```

### Como Acessar

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas Prometheus
curl http://localhost:8080/actuator/prometheus

# Info da aplicação
curl http://localhost:8080/actuator/info
```

---

## 🛡️ Headers de Segurança

### Headers Implementados

| Header | Valor | Descrição |
|--------|-------|-----------|
| `X-Content-Type-Options` | `nosniff` | Previne MIME sniffing |
| `X-Frame-Options` | `DENY` | Previne clickjacking |
| `X-XSS-Protection` | `1; mode=block` | Proteção contra XSS |
| `Strict-Transport-Security` | `max-age=31536000; includeSubDomains` | Força HTTPS |
| `Referrer-Policy` | `strict-origin-when-cross-origin` | Controla referrer |
| `Permissions-Policy` | `camera=(), microphone=(), geolocation=()` | Restringe features |
| `X-Correlation-ID` | UUID único | Rastreamento de requisições |
| `X-Response-Time` | Tempo em ms | Tempo de processamento |

### Exemplo de Resposta

```
HTTP/1.1 200 OK
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: camera=(), microphone=(), geolocation=()
X-Correlation-ID: abc-123-def-456
X-Response-Time: 145
Content-Type: application/json
```

---

## 🔐 CORS (Cross-Origin Resource Sharing)

### Origens Permitidas

```java
allowedOrigins: [
  "http://localhost:4200",    // Dev Angular
  "http://localhost:4300",    // Dev MeuMinisterio
  "https://icertag.com.br",   // Produção
  "https://www.icertag.com.br" // Produção WWW
]
```

### Métodos Permitidos

```
GET, POST, PUT, DELETE, PATCH, OPTIONS
```

### Headers Permitidos

```
* (todos os headers)
```

---

## 📊 Monitoring Stack Recomendado

### Stack Completa

```
┌─────────────────────────────────────────────────────────┐
│                    SGE Backend                           │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │ Rate Limit  │  │  Logging    │  │  Metrics    │    │
│  │ (Bucket4j)  │  │ (Logback)   │  │ (Micrometer)│    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │
│         │                │                │            │
└─────────┼────────────────┼────────────────┼────────────┘
          │                │                │
          ▼                ▼                ▼
   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
   │   Redis     │  │  ELK Stack  │  │ Prometheus  │
   │ (Cache/     │  │ (Elastic,   │  │ + Grafana   │
   │  Buckets)   │  │  Logstash,  │  │             │
   │             │  │  Kibana)    │  │             │
   └─────────────┘  └─────────────┘  └─────────────┘
```

### Configuração Prometheus

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'sge-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Dashboard Grafana Recomendado

1. **HTTP Requests** - Taxa de requisições por endpoint
2. **Response Time** - Latência p50, p95, p99
3. **Error Rate** - Taxa de erros 4xx e 5xx
4. **JVM Metrics** - Memória, GC, threads
5. **Business Metrics** - Logins, CRUD operations

---

## 🚀 Como Rodar

### 1. Iniciar Backend

```bash
cd sg-backend
mvn spring-boot:run
```

### 2. Verificar Health

```bash
# Health check
curl http://localhost:8080/actuator/health

# Métricas
curl http://localhost:8080/actuator/prometheus
```

### 3. Logs

```bash
# Logs gerais
tail -f logs/sge-backend.log

# Logs JSON
tail -f logs/sge-backend.json

# Logs de segurança
tail -f logs/sge-security.log
```

---

## 🔧 Configuração

### Variáveis de Ambiente

```bash
# Rate Limiting
RATE_LIMIT_LOGIN=5
RATE_LIMIT_GENERAL=120

# Actuator
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE=health,info,metrics,prometheus

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_SG=DEBUG
```

### application.yml

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.sg: DEBUG
  file:
    name: logs/sge-backend.log
```

---

## 📈 Métricas de Segurança

### Monitoramento em Tempo Real

| Métrica | Descrição | Alerta |
|---------|-----------|--------|
| `sge_login_failure_total` | Falhas de login | > 10/minuto |
| `sge_errors_total` | Erros 5xx | > 5/minuto |
| `sge_rate_limited_total` | Requisições bloqueadas | > 50/minuto |
| `sge_request_duration_seconds` | Latência p99 | > 2 segundos |

### Logs de Auditoria

```bash
# Usuários que criaram registros
grep "ACTION=CREATE" logs/sge-audit.log

# Registros deletados
grep "ACTION=DELETE" logs/sge-audit.log

# Falhas de login
grep "LOGIN_FAILED" logs/sge-security.log
```

---

## ✅ Checklist de Segurança

- [x] Rate limiting em endpoints públicos
- [x] Headers de segurança (HSTS, X-Content-Type-Options, etc.)
- [x] CORS configurado com origens explícitas
- [x] Logs estruturados para auditoria
- [x] Health checks com detalhes
- [x] Métricas para monitoramento
- [x] Validação de token em todos os endpoints
- [x] Senhas hasheadas com BCrypt
- [x] Senhas temporárias obrigatórias
- [x] Rotas admin protegidas por ROLE

---

## 📚 Referências

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer](https://micrometer.io/docs)
- [Bucket4j](https://bucket4j.com/)
- [OWASP Security Headers](https://owasp.org/www-project-secure-headers/)
- [Prometheus](https://prometheus.io/docs/)
- [Grafana](https://grafana.com/docs/)

---

**Última atualização:** Agosto 2026
**Responsável:** Equipe de Desenvolvimento SGE
