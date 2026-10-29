# 🔒 Análise de Segurança - Proteção de Imagens Uploaded

**Data:** 27 de Outubro de 2026  
**Problema:** URLs de imagens são acessíveis publicamente sem autenticação

---

## 🚨 Vulnerabilidade Identificada

### **Problema Atual:**
```
URL Pública (acessível por qualquer pessoa):
https://api.icertag.com.br/api/uploads/2026/09/18/b6535640-3308-4393-8c7e-ad7f0e592353.png

❌ Qualquer pessoa com a URL pode acessar a foto
❌ Não requer autenticação
❌ Pode ser compartilhada e vazada
❌ Scrapers podem baixar todas as imagens
❌ Possível enumeração de arquivos (força bruta)
```

### **Impacto:**
- ⚠️ **Privacidade:** Fotos de perfil podem ser acessadas por não-autenticados
- ⚠️ **Vazamento de dados:** URLs podem ser compartilhadas em fóruns/redes sociais
- ⚠️ **Scraping:** Bots podem baixar todas as imagens do sistema
- ⚠️ **Compliance:** Pode violar LGPD/GDPR dependendo do tipo de dado

---

## 💡 5 Soluções Propostas

### **Opção 1: Signed URLs (Assinatura Temporária)** ⭐ RECOMENDADO

**Como funciona:**
- URL tem um **token** único e **expirável** (ex: válido por 1 hora)
- Token é gerado com **HMAC** usando chave secreta
- Servidor valida o token antes de servir a imagem

**Exemplo:**
```
ANTES:
https://api.icertag.com.br/api/uploads/2026/09/18/foto.png

DEPOIS:
https://api.icertag.com.br/api/uploads/2026/09/18/foto.png?token=abc123&expires=1698432000

Token inválido/expirado → 403 Forbidden
```

**Vantagens:**
- ✅ URLs expiram automaticamente (segurança temporal)
- ✅ Não requer mudanças no frontend (só adiciona parâmetros)
- ✅ Funciona com cache/CDN
- ✅ Implementação simples

**Desvantagens:**
- ⚠️ URLs podem ser compartilhadas durante o período de validade
- ⚠️ Precisa renovar tokens periodicamente

**Nível de Segurança:** ⭐⭐⭐⭐ (Alto)

---

### **Opção 2: Autenticação Obrigatória (JWT/Session)**

**Como funciona:**
- Endpoint `/api/uploads/**` **requer autenticação**
- Verifica token JWT ou sessão antes de servir arquivo
- Só usuários logados podem acessar

**Exemplo:**
```java
@GetMapping("/**")
public ResponseEntity<Resource> serveFile(
    HttpServletRequest request,
    @RequestHeader(value = "Authorization", required = false) String authHeader
) {
    // Valida JWT
    if (!jwtService.isValid(authHeader)) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    // Serve arquivo apenas para autenticados
    // ...
}
```

**Vantagens:**
- ✅ Proteção total (apenas autenticados)
- ✅ Controle granular de permissões
- ✅ Auditoria de acesso (quem acessou qual arquivo)

**Desvantagens:**
- ❌ Não funciona com tags `<img>` diretas (sem JS)
- ❌ Problemas com cache de navegador
- ❌ Não funciona com CDN pública

**Nível de Segurança:** ⭐⭐⭐⭐⭐ (Muito Alto)

---

### **Opção 3: URLs Ofuscadas com Hash** 🔐

**Como funciona:**
- Arquivo salvo com **hash único** não-previsível
- URL não contém data nem nome original
- Dificulta enumeração

**Exemplo:**
```
ANTES (previsível):
/uploads/2026/09/18/b6535640-3308-4393-8c7e-ad7f0e592353.png

DEPOIS (ofuscado):
/uploads/a7f3e9d2c1b8f4a6e5d3c2b1a9f8e7d6c5b4a3f2e1d0c9b8a7f6e5d4c3b2a1.png
```

**Vantagens:**
- ✅ Dificulta adivinhação de URLs
- ✅ Não requer mudanças no fluxo de autenticação
- ✅ Funciona com cache/CDN

**Desvantagens:**
- ⚠️ **Security through obscurity** (não é segurança real!)
- ⚠️ Se a URL vazar, continua acessível
- ⚠️ Não protege contra compartilhamento

**Nível de Segurança:** ⭐⭐ (Baixo - não recomendado como única solução)

---

### **Opção 4: Proxy com Autenticação (Image Proxy)**

**Como funciona:**
- Frontend **nunca** acessa `/api/uploads/` diretamente
- Todas as imagens passam por um **endpoint proxy** autenticado
- Proxy valida permissão e retorna imagem

**Exemplo:**
```
Frontend solicita:
GET /api/image-proxy?file=2026/09/18/foto.png
Headers: Authorization: Bearer <JWT>

Backend:
1. Valida JWT
2. Verifica se usuário tem permissão para ver esse arquivo
3. Lê arquivo do disco
4. Retorna bytes da imagem
```

**Vantagens:**
- ✅ Controle total de acesso
- ✅ Auditoria completa
- ✅ Pode aplicar transformações (resize, watermark)
- ✅ Funciona com tags `<img>` se usar cookies

**Desvantagens:**
- ⚠️ Aumenta carga do servidor (sem cache direto)
- ⚠️ Latência maior
- ⚠️ Não escala bem sem CDN

**Nível de Segurança:** ⭐⭐⭐⭐⭐ (Muito Alto)

---

### **Opção 5: CDN com Assinatura (Cloudflare/AWS)** ☁️

**Como funciona:**
- Arquivos armazenados em **CDN** (Cloudflare R2, AWS S3)
- URLs geradas com **assinatura temporária**
- CDN valida assinatura antes de servir

**Exemplo (AWS S3):**
```java
// Gera URL assinada válida por 1 hora
String presignedUrl = s3Client.generatePresignedUrl(
    "bucket-name",
    "uploads/foto.png",
    Date.from(Instant.now().plus(1, ChronoUnit.HOURS)),
    HttpMethod.GET
);

// URL: https://bucket.s3.amazonaws.com/foto.png?AWSAccessKeyId=...&Expires=1698432000&Signature=...
```

**Vantagens:**
- ✅ Performance máxima (CDN global)
- ✅ Segurança forte (assinatura criptográfica)
- ✅ Escalabilidade infinita
- ✅ Reduz carga do servidor

**Desvantagens:**
- ⚠️ Custo adicional (CDN)
- ⚠️ Dependência de serviço externo
- ⚠️ Complexidade de migração

**Nível de Segurança:** ⭐⭐⭐⭐⭐ (Muito Alto + Performance)

---

## 📊 Tabela Comparativa

| Solução | Segurança | Performance | Complexidade | Custo | Cache/CDN | Recomendação |
|---------|-----------|-------------|--------------|-------|-----------|--------------|
| **1. Signed URLs** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ Baixa | 💰 Grátis | ✅ Sim | ✅ **RECOMENDADO** |
| **2. Auth Obrigatória** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐ Média | 💰 Grátis | ❌ Não | ⚠️ Casos específicos |
| **3. URLs Ofuscadas** | ⭐⭐ | ⭐⭐⭐⭐ | ⭐ Muito Baixa | 💰 Grátis | ✅ Sim | ❌ NÃO USAR SOZINHO |
| **4. Image Proxy** | ⭐⭐⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ Alta | 💰 Grátis | ⚠️ Parcial | ⚠️ Alto controle |
| **5. CDN Assinado** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ Alta | 💰💰 Pago | ✅ Sim | 🚀 Produção em escala |

---

## ✅ Recomendação: **Opção 1 + Opção 3 (Híbrido)**

### **Implementação Recomendada:**

1. **Curto Prazo (Imediato):**
   - ✅ URLs Ofuscadas (hash não-previsível)
   - ✅ Signed URLs com expiração de 24 horas
   - ✅ Rate limiting no endpoint de uploads

2. **Médio Prazo:**
   - ✅ Adicionar permissões de acesso (quem pode ver qual foto)
   - ✅ Auditoria de acessos (log de downloads)

3. **Longo Prazo (Produção):**
   - ✅ Migrar para CDN com assinatura (Cloudflare R2 ou AWS S3)
   - ✅ Implementar watermark dinâmico para fotos sensíveis

---

## 🛠️ Implementação Prática

### **Opção 1: Signed URLs (Implementação Imediata)**

Vou implementar essa solução agora por ser:
- ✅ Mais segura que a atual
- ✅ Simples de implementar
- ✅ Compatível com frontend existente
- ✅ Não quebra nada

**Passos:**
1. Criar serviço de assinatura de URLs
2. Modificar `UploadResourceController` para validar tokens
3. Modificar frontend para adicionar tokens nas URLs
4. Configurar tempo de expiração (24 horas padrão)

---

## 🔐 Detalhes Técnicos - Signed URLs

### **Como Funciona:**

```
1. Frontend solicita foto de perfil
   ↓
2. Backend gera URL assinada:
   - URL original: /api/uploads/2026/09/18/foto.png
   - Adiciona: ?token=HMAC(secretKey, url + expires)&expires=1698432000
   ↓
3. Frontend usa URL completa:
   <img src="/api/uploads/2026/09/18/foto.png?token=abc...&expires=1698432000">
   ↓
4. UploadResourceController valida:
   - Token é válido? (HMAC correto)
   - Não expirou? (expires > agora)
   ↓
5. Se válido: retorna imagem
   Se inválido: 403 Forbidden
```

### **Segurança:**

- 🔒 **HMAC-SHA256:** Token impossível de forjar sem chave secreta
- ⏰ **Expiração:** URLs expiram em 24h (configurável)
- 🔄 **Renovação automática:** Frontend renova token ao carregar página
- 🚫 **Replay attack:** Token vinculado ao caminho do arquivo

---

## 📝 Próximos Passos

Qual opção você prefere implementar?

1. **Opção 1 (Signed URLs)** - ⭐ Recomendado - Implementação rápida (30 min)
2. **Opção 2 (Auth Obrigatória)** - Mais seguro, mas muda comportamento
3. **Opção 4 (Image Proxy)** - Controle total, mas mais complexo
4. **Híbrido (1 + 3)** - Melhor custo-benefício
5. **Outra abordagem** - Me diga suas necessidades

---

**Aguardo sua decisão para implementar a solução escolhida!** 🚀
