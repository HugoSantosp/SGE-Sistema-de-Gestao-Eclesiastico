# 🚀 Configuração de Produção - Signed URLs

## ⚠️ IMPORTANTE: Segurança em Produção

A implementação de Signed URLs requer configuração adicional em produção para garantir máxima segurança.

---

## 🔑 1. Gerar Chave Secreta Forte

### Método 1: OpenSSL (Recomendado)

```bash
# Gerar chave aleatória de 128 caracteres hexadecimais (64 bytes)
openssl rand -hex 64
```

**Saída exemplo:**
```
a7f3e9d2c1b8f4a6e5d3c2b1a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0
e9d8c7b6a5f4e3d2c1b0a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1f0e9d8
```

📋 **Copiar esta chave e guardar em local seguro!**

---

### Método 2: Python

```python
import secrets

# Gerar chave aleatória de 64 bytes
secret_key = secrets.token_hex(64)
print(secret_key)
```

---

### Método 3: Node.js

```javascript
const crypto = require('crypto');

// Gerar chave aleatória de 64 bytes
const secretKey = crypto.randomBytes(64).toString('hex');
console.log(secretKey);
```

---

### Método 4: PowerShell (Windows)

```powershell
# Gerar chave aleatória de 64 bytes
$bytes = New-Object byte[] 64
[System.Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($bytes)
[System.BitConverter]::ToString($bytes) -replace '-','' | Write-Output
```

---

## 🔧 2. Configurar Variável de Ambiente

### 2.1 Linux/macOS (Terminal)

```bash
# Temporário (válido só para a sessão atual)
export APP_SIGNED_URL_SECRET="sua_chave_secreta_aqui"

# Permanente (adicionar ao ~/.bashrc ou ~/.zshrc)
echo 'export APP_SIGNED_URL_SECRET="sua_chave_secreta_aqui"' >> ~/.bashrc
source ~/.bashrc
```

---

### 2.2 Windows (PowerShell)

```powershell
# Temporário (válido só para a sessão atual)
$env:APP_SIGNED_URL_SECRET = "sua_chave_secreta_aqui"

# Permanente (variável de sistema)
[Environment]::SetEnvironmentVariable(
    "APP_SIGNED_URL_SECRET",
    "sua_chave_secreta_aqui",
    "Machine"
)
```

---

### 2.3 Docker / Docker Compose

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  backend:
    image: sge-backend:latest
    environment:
      # Chave secreta (USAR .env OU SECRETS!)
      - APP_SIGNED_URL_SECRET=${APP_SIGNED_URL_SECRET}
      - APP_SIGNED_URL_EXPIRATION_HOURS=24
      - APP_SIGNED_URL_ENABLED=true
    env_file:
      - .env  # ⚠️ NÃO COMMITAR ESTE ARQUIVO!
```

**arquivo .env (criar manualmente):**
```bash
# ⚠️ NUNCA COMMITAR ESTE ARQUIVO NO GIT!
APP_SIGNED_URL_SECRET=a7f3e9d2c1b8f4a6e5d3c2b1a9f8e7d6c5b4a3f2e1d0...
```

**Adicionar ao .gitignore:**
```bash
echo ".env" >> .gitignore
```

---

### 2.4 Kubernetes

**ConfigMap (para configurações não sensíveis):**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: sge-config
data:
  APP_SIGNED_URL_EXPIRATION_HOURS: "24"
  APP_SIGNED_URL_ENABLED: "true"
```

**Secret (para chave secreta):**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: sge-secrets
type: Opaque
data:
  # Base64 encoded (use: echo -n "chave" | base64)
  APP_SIGNED_URL_SECRET: YTdmM2U5ZDJjMWI4ZjRhNmU1ZDNjMmIxYTlmOGU3ZDY=
```

**Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sge-backend
spec:
  template:
    spec:
      containers:
      - name: backend
        image: sge-backend:latest
        envFrom:
        - configMapRef:
            name: sge-config
        env:
        - name: APP_SIGNED_URL_SECRET
          valueFrom:
            secretKeyRef:
              name: sge-secrets
              key: APP_SIGNED_URL_SECRET
```

---

### 2.5 AWS (Elastic Beanstalk / ECS)

**Elastic Beanstalk:**
```bash
# Via CLI
eb setenv APP_SIGNED_URL_SECRET="sua_chave_secreta"

# Ou via console AWS:
# Elastic Beanstalk → Environments → Configuration
# → Software → Environment properties
```

**ECS Task Definition (JSON):**
```json
{
  "containerDefinitions": [
    {
      "environment": [
        {
          "name": "APP_SIGNED_URL_SECRET",
          "value": "sua_chave_secreta"
        }
      ]
    }
  ]
}
```

---

### 2.6 Azure App Service

```bash
# Via CLI
az webapp config appsettings set \
  --name sge-backend \
  --resource-group meu-grupo \
  --settings APP_SIGNED_URL_SECRET="sua_chave_secreta"
```

**Ou via Portal Azure:**
```
App Service → Configuration → Application Settings
→ New application setting
Name: APP_SIGNED_URL_SECRET
Value: sua_chave_secreta
```

---

### 2.7 Google Cloud (Cloud Run / GKE)

**Cloud Run:**
```bash
gcloud run deploy sge-backend \
  --update-env-vars APP_SIGNED_URL_SECRET="sua_chave_secreta"
```

**GKE (usar Secrets do Kubernetes - ver item 2.4)**

---

## 🔒 3. Configurações Adicionais de Segurança

### application-prod.yml (ou application.yml)

```yaml
app:
  signed-url:
    # Habilitar validação (SEMPRE TRUE em produção)
    enabled: true
    
    # Chave secreta (VIA VARIÁVEL DE AMBIENTE!)
    secret: ${APP_SIGNED_URL_SECRET}
    
    # Tempo de expiração (ajustar conforme necessidade)
    expiration-hours: 24  # 24h = 1 dia
    
    # Opções:
    # - 1   = 1 hora (documentos muito sensíveis)
    # - 8   = 8 horas (jornada de trabalho)
    # - 24  = 1 dia (padrão recomendado)
    # - 168 = 7 dias (assets públicos)

# HTTPS obrigatório em produção
server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12

# Rate limiting (prevenir brute force)
bucket4j:
  enabled: true
  filters:
  - url: /api/uploads/.*
    rate-limits:
    - bandwidths:
      - capacity: 100
        time: 1
        unit: minutes
```

---

## 📊 4. Monitoramento e Alertas

### Logs Importantes

**Monitorar estes logs em produção:**

```bash
# Tentativas de acesso sem token
grep "Tentativa de acesso sem assinatura" logs/sge-backend.log

# Tokens inválidos (possível ataque)
grep "Token inválido" logs/sge-backend.log

# URLs expiradas
grep "URL assinada expirada" logs/sge-backend.log
```

---

### Alertas (Exemplo com ELK Stack)

**Alerta 1: Múltiplos acessos sem token (>100/min)**
```json
{
  "query": {
    "match": {
      "message": "Tentativa de acesso sem assinatura"
    }
  },
  "threshold": 100,
  "window": "1m",
  "action": "send_email"
}
```

**Alerta 2: Múltiplos tokens inválidos do mesmo IP**
```json
{
  "query": {
    "bool": {
      "must": [
        { "match": { "message": "Token inválido" } },
        { "term": { "clientIp": "{{ip}}" } }
      ]
    }
  },
  "threshold": 50,
  "window": "5m",
  "action": "block_ip"
}
```

---

## 🧪 5. Validação em Produção

### Checklist Pré-Deploy

- [ ] ✅ Chave secreta forte gerada (min 64 caracteres)
- [ ] ✅ Variável de ambiente configurada
- [ ] ✅ `.env` adicionado ao `.gitignore`
- [ ] ✅ Chave secreta NÃO commitada no código
- [ ] ✅ `app.signed-url.enabled=true`
- [ ] ✅ HTTPS configurado
- [ ] ✅ Rate limiting ativado
- [ ] ✅ Logs configurados
- [ ] ✅ Alertas configurados

---

### Testes Pós-Deploy

**1. Testar acesso sem token:**
```bash
curl -I https://api.icertag.com.br/api/uploads/teste.png

# Esperado: HTTP/1.1 403 Forbidden
```

**2. Testar com token válido:**
```bash
# 1. Fazer login
TOKEN=$(curl -X POST https://api.icertag.com.br/auth/login \
  -H "Content-Type: application/json" \
  -d '{"user":"admin@example.com","senha":"senha"}' \
  | jq -r '.token')

# 2. Obter URL assinada
FOTO_URL=$(curl https://api.icertag.com.br/auth/me \
  -H "Authorization: Bearer $TOKEN" \
  | jq -r '.foto')

# 3. Acessar foto
curl -I "https://api.icertag.com.br${FOTO_URL}"

# Esperado: HTTP/1.1 200 OK
```

**3. Testar token forjado:**
```bash
curl -I "https://api.icertag.com.br/api/uploads/teste.png?token=INVALIDO&expires=9999999999"

# Esperado: HTTP/1.1 403 Forbidden
```

---

## 🔐 6. Rotação de Chaves (Segurança Avançada)

### Quando Rotacionar

- ✅ A cada 90 dias (recomendado)
- ✅ Se chave foi comprometida
- ✅ Quando funcionário com acesso sair da empresa
- ✅ Após incidente de segurança

---

### Processo de Rotação (Zero Downtime)

**Estratégia: Dupla Validação Temporária**

1. **Gerar nova chave:**
   ```bash
   openssl rand -hex 64
   ```

2. **Configurar chave antiga + nova (transição):**
   ```yaml
   app:
     signed-url:
       secret: ${APP_SIGNED_URL_SECRET}
       secret-old: ${APP_SIGNED_URL_SECRET_OLD}  # Aceitar ambas temporariamente
   ```

3. **Modificar `validateSignedUrl()` para aceitar ambas:**
   ```java
   public boolean validateSignedUrl(...) {
       boolean validWithNew = validateWithKey(filePath, token, expires, secretKey);
       boolean validWithOld = validateWithKey(filePath, token, expires, secretKeyOld);
       return validWithNew || validWithOld;
   }
   ```

4. **Deploy da nova versão:**
   - URLs antigas (geradas com chave antiga) continuam funcionando
   - URLs novas (geradas com chave nova) também funcionam

5. **Aguardar expiração das URLs antigas (24h)**

6. **Remover chave antiga:**
   ```yaml
   app:
     signed-url:
       secret: ${APP_SIGNED_URL_SECRET}
       # secret-old removido
   ```

7. **Deploy final**

---

## 📋 7. Configurações Avançadas

### Diferentes Expirações por Tipo de Arquivo

```yaml
app:
  signed-url:
    expiration:
      default: 24  # 24h padrão
      profile-photos: 48  # 2 dias (atualizadas raramente)
      documents: 1  # 1h (sensíveis)
      reports: 8  # 8h (jornada de trabalho)
      public-assets: 168  # 7 dias (imagens públicas)
```

**Modificar `SignedUrlService`:**
```java
public String generateSignedUrl(String filePath, FileType type) {
    int hours = getExpirationForType(type);
    long expiresAt = Instant.now().getEpochSecond() + (hours * 3600L);
    // ...
}
```

---

### Vincular Token ao IP do Cliente (Extra Seguro)

```java
public String generateSignedUrl(String filePath, String clientIp) {
    String message = filePath + "|" + expiresAt + "|" + clientIp;
    // Gerar HMAC incluindo IP
}

public boolean validateSignedUrl(String filePath, String token, 
                                  Long expiresAt, String clientIp) {
    String expectedToken = generateToken(filePath, expiresAt, clientIp);
    // Validar incluindo IP
}
```

**⚠️ Cuidado:** Usuários com IP dinâmico terão problemas.

---

### Whitelist de Domínios (CORS)

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Apenas domínios autorizados
        config.setAllowedOrigins(Arrays.asList(
            "https://app.icertag.com.br",
            "https://painel.icertag.com.br"
        ));
        
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        source.registerCorsConfiguration("/api/uploads/**", config);
        return new CorsFilter(source);
    }
}
```

---

## 🚨 8. Troubleshooting em Produção

### Problema: "Chave secreta não configurada"

**Erro:**
```
java.lang.IllegalStateException: APP_SIGNED_URL_SECRET not configured
```

**Solução:**
```bash
# Verificar se variável está definida
echo $APP_SIGNED_URL_SECRET

# Se vazio, configurar:
export APP_SIGNED_URL_SECRET="sua_chave"

# Reiniciar aplicação
```

---

### Problema: Todos os acessos retornam 403

**Causa:** Chave diferente entre geração e validação

**Solução:**
```bash
# 1. Verificar chave configurada
printenv | grep APP_SIGNED_URL_SECRET

# 2. Regenerar URL assinada (novo login)
curl -X POST https://api.icertag.com.br/auth/login ...

# 3. Verificar logs
tail -f logs/sge-backend.log | grep "Token inválido"
```

---

### Problema: Performance degradada

**Causa:** HMAC em cada request pode ser otimizado

**Solução:**
```java
// Cache de tokens já validados (TTL curto)
@Cacheable(value = "signedUrls", key = "#token")
public boolean validateSignedUrl(...) {
    // ...
}
```

**application.yml:**
```yaml
spring:
  cache:
    cache-names: signedUrls
    caffeine:
      spec: maximumSize=10000,expireAfterWrite=5m
```

---

## ✅ Checklist Final de Produção

- [ ] ✅ Chave secreta forte (min 64 caracteres hexadecimais)
- [ ] ✅ Variável de ambiente configurada (não hardcoded)
- [ ] ✅ `.env` no `.gitignore`
- [ ] ✅ HTTPS ativado
- [ ] ✅ Rate limiting configurado
- [ ] ✅ CORS configurado (apenas domínios autorizados)
- [ ] ✅ Logs estruturados
- [ ] ✅ Alertas configurados (acessos sem token, tokens inválidos)
- [ ] ✅ Backup da chave secreta em local seguro
- [ ] ✅ Documentação de rotação de chaves
- [ ] ✅ Testes pós-deploy executados
- [ ] ✅ Monitoramento ativo

---

## 📞 Suporte

**Dúvidas sobre configuração de produção?**

1. 📖 Ler documentação completa: `IMPLEMENTACAO_SIGNED_URLS.md`
2. 🧪 Executar testes: `bash teste-signed-urls.sh`
3. 🔍 Verificar logs do backend
4. 💬 Reportar problema com contexto

---

**Configurado em:** 27/10/2026  
**Versão:** 1.0  
**Status:** ✅ **PRONTO PARA PRODUÇÃO**
