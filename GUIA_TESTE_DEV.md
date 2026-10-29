# 🧪 Guia de Teste em DEV (Local)

## ⚡ Teste Rápido (5 minutos)

### **1️⃣ Gerar Chave para DEV**

Execute no PowerShell ou CMD:

```bash
python gerar-chave-secreta.py
```

📋 **Copie a chave gerada**

---

### **2️⃣ Configurar Variável de Ambiente**

#### **PowerShell:**

```powershell
# Configurar variável (TEMPORÁRIA - só para esta sessão)
$env:APP_SIGNED_URL_SECRET = "sua_chave_gerada_aqui"

# Verificar se foi configurada
echo $env:APP_SIGNED_URL_SECRET
```

#### **CMD (Prompt de Comando):**

```cmd
set APP_SIGNED_URL_SECRET=sua_chave_gerada_aqui
echo %APP_SIGNED_URL_SECRET%
```

---

### **3️⃣ Iniciar Backend**

**No mesmo terminal onde configurou a variável:**

```bash
cd sg-backend
./mvnw spring-boot:run
```

⏳ **Aguardar log:**
```
Started SgeBackendApplication in X.XXX seconds
```

---

### **4️⃣ Executar Testes Automatizados**

**Em outro PowerShell (novo terminal):**

```powershell
.\teste-dev-signed-urls.ps1
```

**Ou manualmente com curl:**

```bash
# Teste 1: Sem token (deve retornar 403)
curl -I http://localhost:8080/api/uploads/teste.png

# Esperado: HTTP/1.1 403 Forbidden
```

---

## ✅ **Resultado Esperado**

```
========================================
📊 Resumo dos Testes
========================================

✅ Teste 1: Acesso sem token bloqueado (403)
✅ Teste 2: Login funcionando
✅ Teste 3: URL assinada gerada corretamente
✅ Teste 4: Acesso com token válido (200 ou 404*)
✅ Teste 5: Acesso sem token bloqueado (403)

*404 é normal se usuário não tem foto
========================================
```

---

## 🐛 **Se Algo der Errado**

### **Problema 1: "Todos os acessos retornam 200 (sem verificar token)"**

**Causa:** `app.signed-url.enabled=false` no `application.yml`

**Solução:** Verificar `sg-backend/src/main/resources/application.yml`:

```yaml
app:
  signed-url:
    enabled: true  # ← Deve estar TRUE
```

---

### **Problema 2: "Backend não inicia"**

**Causa:** Variável de ambiente não foi configurada

**Solução:** Verificar variável:

```powershell
echo $env:APP_SIGNED_URL_SECRET
```

Se vazio, configurar novamente e reiniciar backend.

---

### **Problema 3: "Login falha (401)"**

**Causa:** Usuário admin não existe

**Solução:** Criar usuário via SQL ou usar outro email/senha existente.

---

## 🚀 **Após Testes Passarem**

1. ✅ Parar backend (Ctrl+C)
2. ✅ Fazer commit das mudanças:
   ```bash
   git add .
   git commit -m "feat: implementação de Signed URLs para segurança de imagens"
   git push
   ```
3. ✅ Ir no Dockploy e fazer Deploy
4. ✅ Testar em produção

---

## 📝 **Comandos Úteis**

```powershell
# Ver variáveis de ambiente
Get-ChildItem Env: | Where-Object { $_.Name -like "*SIGNED*" }

# Matar processo do backend (se travar)
taskkill /F /IM java.exe

# Ver logs do backend em tempo real
tail -f sg-backend/logs/sge-backend.log
```

---

**Tempo estimado:** 5 minutos  
**Dificuldade:** Fácil  
**Pré-requisitos:** Java, Maven, PowerShell
