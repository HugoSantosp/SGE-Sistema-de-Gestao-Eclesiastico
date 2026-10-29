# 🧪 Teste Rápido - Signed URLs

## ⚡ Testes em 5 Minutos

### 1️⃣ Compilar e Iniciar Backend

```bash
cd sg-backend
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

Aguardar log:
```
✅ Started SgeBackendApplication in X.XXX seconds
```

---

### 2️⃣ Teste com cURL

#### ❌ Teste 1: Acesso SEM token (deve falhar com 403)

```bash
curl -I http://localhost:8080/api/uploads/2026/09/18/test.png
```

**Resultado Esperado:**
```
HTTP/1.1 403 Forbidden
```

**Log Backend:**
```
WARN - Tentativa de acesso sem assinatura: 2026/09/18/test.png
```

---

#### ✅ Teste 2: Login e obter URL assinada

**2.1 - Fazer Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "user": "admin@example.com",
    "senha": "admin123"
  }'
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "nome": "Administrador",
  ...
}
```

📝 **Copiar o valor de `token`** (salvar como `$JWT_TOKEN`)

---

**2.2 - Obter Perfil com Foto:**
```bash
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer SEU_JWT_TOKEN_AQUI"
```

**Resposta:**
```json
{
  "id": 1,
  "nome": "Administrador",
  "email": "admin@example.com",
  "foto": "/api/uploads/2026/09/18/admin-foto.png?token=abc123def456&expires=1730073600",
  ...
}
```

📝 **Copiar o valor completo de `foto`** (salvar como `$FOTO_URL`)

---

**2.3 - Acessar Foto com Token:**
```bash
curl -I "http://localhost:8080$FOTO_URL"
```

**Resultado Esperado:**
```
HTTP/1.1 200 OK
Content-Type: image/png
Content-Disposition: inline
```

✅ **SUCESSO!** Imagem foi servida com autenticação.

---

#### 🔨 Teste 3: Forjar token (deve falhar com 403)

```bash
# Modificar o token na URL
curl -I "http://localhost:8080/api/uploads/2026/09/18/admin-foto.png?token=TOKEN_INVALIDO&expires=9999999999"
```

**Resultado Esperado:**
```
HTTP/1.1 403 Forbidden
```

**Log Backend:**
```
WARN - Token inválido para arquivo: 2026/09/18/admin-foto.png
```

---

#### ⏰ Teste 4: Token expirado (deve falhar com 403)

```bash
# Usar expires no passado (ex: 1000000000 = 09/09/2001)
curl -I "http://localhost:8080/api/uploads/2026/09/18/admin-foto.png?token=abc123&expires=1000000000"
```

**Resultado Esperado:**
```
HTTP/1.1 403 Forbidden
```

**Log Backend:**
```
WARN - URL assinada expirada. Agora: 1730073000, Expiração: 1000000000
```

---

### 3️⃣ Teste com Postman

**Importar Collection:**

```json
{
  "info": {
    "name": "Signed URLs - Test",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Login",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/auth/login",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"user\": \"admin@example.com\",\n  \"senha\": \"admin123\"\n}",
          "options": { "raw": { "language": "json" } }
        }
      }
    },
    {
      "name": "2. Obter Perfil (URL Assinada)",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/auth/me",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          }
        ]
      }
    },
    {
      "name": "3. Acessar Foto COM Token (200 OK)",
      "request": {
        "method": "GET",
        "url": "{{foto_url_com_token}}"
      }
    },
    {
      "name": "4. Acessar Foto SEM Token (403 Forbidden)",
      "request": {
        "method": "GET",
        "url": "http://localhost:8080/api/uploads/2026/09/18/admin-foto.png"
      }
    }
  ]
}
```

**Passos:**
1. ▶️ Executar request "1. Login"
2. 📋 Copiar valor de `token` para variável `jwt_token`
3. ▶️ Executar request "2. Obter Perfil"
4. 📋 Copiar valor de `foto` para variável `foto_url_com_token`
5. ▶️ Executar request "3. Acessar Foto COM Token"
   - ✅ Deve retornar **200 OK** + imagem
6. ▶️ Executar request "4. Acessar Foto SEM Token"
   - ❌ Deve retornar **403 Forbidden**

---

### 4️⃣ Teste no Navegador

**4.1 - Fazer Login no Frontend:**
```
http://localhost:4200/login
```

**4.2 - Ir para Perfil:**
```
http://localhost:4200/perfil
```

**4.3 - Inspecionar a Tag `<img>`:**
```html
<!-- Abrir DevTools (F12) → Inspector/Elements -->
<img src="/api/uploads/2026/09/18/foto.png?token=abc123&expires=1730073600" />
```

✅ **Foto deve carregar normalmente**

**4.4 - Copiar URL da imagem e abrir em aba anônima:**
```
1. Botão direito na foto → "Copiar endereço da imagem"
2. Abrir aba anônima (Ctrl+Shift+N)
3. Colar URL na barra de endereços
```

✅ **Deve mostrar a imagem** (token ainda válido)

**4.5 - Remover token da URL e tentar acessar:**
```
http://localhost:8080/api/uploads/2026/09/18/foto.png
(sem ?token=...&expires=...)
```

❌ **Deve mostrar erro 403 Forbidden**

---

### 5️⃣ Testes Unitários (JUnit)

```bash
cd sg-backend
./mvnw test -Dtest=SignedUrlServiceTest
```

**Resultado Esperado:**
```
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Testes Executados:**
- ✅ Geração de URL assinada
- ✅ Validação de token válido
- ✅ Rejeição de token inválido
- ✅ Rejeição de token expirado
- ✅ Normalização de caminhos (com/sem barra)
- ✅ Proteção contra null parameters
- ✅ Verificação de expiração futura
- ✅ Tokens diferentes para chamadas diferentes
- ✅ Token de arquivo A não funciona em arquivo B

---

## 📊 Checklist de Validação

Marque cada teste executado:

### Backend
- [ ] ✅ Backend inicia sem erros
- [ ] ✅ Configuração `app.signed-url` carregada corretamente
- [ ] ✅ GET `/api/uploads/foto.png` (sem token) → **403 Forbidden**
- [ ] ✅ Logs mostram "Tentativa de acesso sem assinatura"

### Autenticação
- [ ] ✅ POST `/auth/login` retorna JWT
- [ ] ✅ GET `/auth/me` retorna perfil com campo `foto`
- [ ] ✅ Campo `foto` contém `?token=...&expires=...`

### URLs Assinadas
- [ ] ✅ URL com token válido → **200 OK**
- [ ] ✅ URL sem token → **403 Forbidden**
- [ ] ✅ URL com token inválido → **403 Forbidden**
- [ ] ✅ URL com token expirado → **403 Forbidden**

### Frontend (Opcional)
- [ ] ✅ Login funciona
- [ ] ✅ Foto de perfil carrega corretamente
- [ ] ✅ Tag `<img>` contém token na URL
- [ ] ✅ Abrir URL diretamente (sem token) → erro 403

### Testes Automatizados
- [ ] ✅ Todos os testes unitários passam (11/11)
- [ ] ✅ Sem warnings de segurança

---

## 🐛 Problemas Comuns

### ❌ "Bean SignedUrlService not found"

**Causa:** Spring não encontrou o serviço

**Solução:**
```bash
# Recompilar
./mvnw clean install
```

---

### ❌ "403 Forbidden" mesmo com token

**Causa:** Chave secreta diferente entre geração e validação

**Solução:**
```bash
# Verificar configuração
grep -r "signed-url.secret" sg-backend/src/main/resources/

# Garantir que é a mesma chave em dev/prod
```

---

### ❌ Token expira imediatamente

**Causa:** Relógio do sistema desajustado

**Solução:**
```bash
# Verificar timestamp atual
date +%s

# Comparar com expires retornado pelo backend
# Diferença deve ser ~86400 (24h)
```

---

### ❌ Frontend não mostra imagem

**Causa:** CORS ou URL mal formatada

**Solução:**
1. Abrir DevTools → Network
2. Verificar requisição para `/api/uploads/...`
3. Checar status code (deve ser 200, não 403)
4. Se 403, verificar se token está presente na URL

---

## 🎯 Resultado Final Esperado

### ✅ Tudo Funcionando:

```
🔒 ANTES (Inseguro):
   ❌ /api/uploads/foto.png → 200 OK (qualquer pessoa acessa)

🔐 DEPOIS (Seguro):
   ❌ /api/uploads/foto.png → 403 Forbidden
   ✅ /api/uploads/foto.png?token=abc&expires=123 → 200 OK
   ⏰ Token expira em 24h automaticamente
   🛡️ Impossível forjar sem chave secreta
```

---

## 🚀 Próximo Passo

Se todos os testes passaram:

✅ **Deploy em ambiente de staging/teste**

```bash
# 1. Gerar chave secreta forte
openssl rand -hex 64

# 2. Configurar variável de ambiente
export APP_SIGNED_URL_SECRET="chave_gerada_acima"

# 3. Build para produção
./mvnw clean package -DskipTests

# 4. Deploy
java -jar target/sge-backend-*.jar
```

---

## 📞 Suporte

Se algum teste falhar:

1. 📋 Copiar mensagem de erro completa
2. 📄 Anexar logs do backend
3. 🔍 Verificar arquivo `IMPLEMENTACAO_SIGNED_URLS.md` (seção Troubleshooting)
4. 💬 Reportar problema com contexto

---

**Tempo estimado de testes:** 5-10 minutos  
**Status:** ✅ Pronto para testar
