# 🎯 Passo a Passo: Testar em DEV

## 📋 Checklist Simples

### ✅ **PASSO 1: Gerar Chave**

Abra PowerShell e execute:

```powershell
python gerar-chave-secreta.py
```

**Resultado:**
```
🔐 Chave Secreta Gerada com Sucesso!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

78953dee17d899cca3f488d4795657b1085a7bbc82d09c6c96f56429aaf2ae51...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

📋 **Copiar esta chave** (Ctrl+C)

---

### ✅ **PASSO 2: Configurar Variável**

**No mesmo PowerShell**, execute:

```powershell
$env:APP_SIGNED_URL_SECRET = "COLAR_CHAVE_AQUI"
```

**Exemplo completo:**
```powershell
$env:APP_SIGNED_URL_SECRET = "COLE_A_CHAVE_APP_SIGNED_URL_SECRET_AQUI"
```

**Verificar:**
```powershell
echo $env:APP_SIGNED_URL_SECRET
```

Deve mostrar a chave completa. ✅

---

### ✅ **PASSO 3: Iniciar Backend**

**No mesmo PowerShell** (importante!), execute:

```powershell
cd sg-backend
./mvnw spring-boot:run
```

**Aguardar ver:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::

...
Started SgeBackendApplication in 8.234 seconds (JVM running for 9.123)
```

✅ **Backend rodando!**

**NÃO FECHE ESTE TERMINAL!**

---

### ✅ **PASSO 4: Testar**

**Abra um NOVO PowerShell** (deixe o backend rodando no outro) e execute:

```powershell
curl -I http://localhost:8080/api/uploads/teste.png
```

**Resultado esperado:**
```
HTTP/1.1 403 Forbidden
```

---

### ✅ **PASSO 5: Teste Completo (Opcional)**

No segundo PowerShell, execute:

```powershell
.\teste-dev-signed-urls.ps1
```

**Vai testar:**
- ✅ Acesso sem token (403)
- ✅ Login
- ✅ Geração de URL assinada
- ✅ Acesso com token válido
- ✅ Acesso sem token (403)

---

## 🎉 **Se Tudo Funcionou**

```
✅ Backend iniciou sem erros
✅ Acesso sem token retorna 403
✅ Login funciona
✅ URL assinada gerada corretamente
```

**Próximo passo:**

1. Parar backend (Ctrl+C no primeiro terminal)
2. Fazer commit:
   ```bash
   git add .
   git commit -m "feat: Signed URLs implementadas"
   git push
   ```
3. Deploy no Dockploy ✅

---

## ❌ **Se Algo der Errado**

### **Erro 1: "Backend não inicia"**

**Solução:**
```powershell
# Verificar se variável está configurada
echo $env:APP_SIGNED_URL_SECRET

# Se vazio, configurar novamente
$env:APP_SIGNED_URL_SECRET = "sua_chave"

# Tentar novamente
cd sg-backend
./mvnw spring-boot:run
```

---

### **Erro 2: "curl não reconhecido"**

**Solução 1 - Usar Invoke-WebRequest:**
```powershell
(Invoke-WebRequest -Uri http://localhost:8080/api/uploads/teste.png -Method HEAD -ErrorAction SilentlyContinue).StatusCode
```

**Solução 2 - Instalar curl:**
```powershell
winget install curl
```

---

### **Erro 3: "Acesso retorna 200 (não bloqueia)"**

**Causa:** `signed-url.enabled=false`

**Solução:**
```yaml
# Editar: sg-backend/src/main/resources/application.yml
app:
  signed-url:
    enabled: true  # ← Mudar para true
```

Reiniciar backend.

---

## 📞 **Precisa de Ajuda?**

1. Verificar logs do backend no terminal
2. Procurar por erros em vermelho
3. Ver arquivo: `GUIA_TESTE_DEV.md` (mais detalhes)
4. Reportar erro com print dos logs

---

**Tempo estimado:** 5 minutos  
**Dificuldade:** ⭐ Fácil
