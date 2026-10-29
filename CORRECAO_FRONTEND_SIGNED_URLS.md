# ✅ Correção Frontend - Signed URLs

## 🐛 **Problema Identificado**

O frontend estava **destruindo** as URLs assinadas que vinham do backend!

### **O que estava acontecendo:**

```typescript
// Backend retornava:
"/api/uploads/foto.png?token=abc123&expires=1730073600"

// Frontend (método getFotoSrc) fazia:
let path = this.foto;  // "/api/uploads/foto.png?token=abc123&expires=1730073600"
path = path.replace(/\/uploads\/+uploads\//g, '/uploads/');  // ❌ Manipulações
// ... mais código

// Resultado final:
"http://localhost:8080/api/uploads/foto.png"  // ❌ PERDEU: ?token=abc&expires=123
```

**Consequência:** Frontend tentava acessar imagem SEM token → **403 Forbidden** ❌

---

## ✅ **Solução Implementada**

Modificados 2 arquivos:

### **1. `perfil.component.ts` (meu-ministerio)**
📁 `sg-frontend/projects/meu-ministerio/src/app/features/perfil/perfil.component.ts`

### **2. `perfil.component.ts` (sge)**
📁 `sg-frontend/src/app/features/auth/perfil/perfil.component.ts`

---

## 🔧 **Mudança Aplicada**

Adicionado verificação no método `getFotoSrc()`:

```typescript
getFotoSrc(): string {
  if (this.fotoPreview) return this.fotoPreview;
  if (this.foto) {
    // Se já começa com http, retorna direto
    if (this.foto.startsWith('http')) return this.foto;
    
    // ✅ NOVO: Verificar se é uma URL assinada (Signed URL)
    if (this.foto.includes('?token=') && this.foto.includes('&expires=')) {
      // URL assinada - PRESERVAR COMPLETA!
      const backendUrl = 'http://localhost:8080';
      const cleanBackendUrl = backendUrl.replace(/\/$/, '');
      
      // Se já começa com /api/, só adicionar domínio
      if (this.foto.startsWith('/api/')) {
        return cleanBackendUrl + this.foto;  // ✅ Preserva ?token=...&expires=...
      }
      
      // Caso contrário, normalizar path antes
      let path = this.foto;
      if (path.startsWith('/uploads/')) {
        path = '/api' + path;
      } else if (path.startsWith('uploads/')) {
        path = '/api/' + path;
      }
      return cleanBackendUrl + path;  // ✅ Preserva ?token=...&expires=...
    }
    
    // URL sem assinatura (backwards compatibility)
    // ... código antigo continua funcionando
  }
  return '/assets/avatar-default.png';
}
```

---

## 🎯 **Como Funciona Agora**

### **Fluxo Completo:**

```
1. Backend retorna:
   {
     "foto": "/api/uploads/2026/09/18/admin.png?token=abc123&expires=1730073600"
   }

2. Frontend detecta: "Tem ?token= e &expires= → É URL assinada!"

3. Frontend preserva URL completa:
   "http://localhost:8080/api/uploads/2026/09/18/admin.png?token=abc123&expires=1730073600"

4. Browser faz request COM token:
   GET http://localhost:8080/api/uploads/2026/09/18/admin.png?token=abc123&expires=1730073600

5. Backend valida token → ✅ 200 OK + Imagem
```

---

## 🧪 **Como Testar**

### **1️⃣ Reiniciar Frontend**

Se o frontend estava rodando, reinicie:

```bash
# Parar (Ctrl+C) e iniciar novamente
cd sg-frontend
ng serve
```

ou

```bash
cd sg-frontend/projects/meu-ministerio
ng serve
```

---

### **2️⃣ Testar no Navegador**

1. **Abrir aplicação:** `http://localhost:4200`
2. **Fazer login**
3. **Ir para página de perfil**
4. **Abrir DevTools (F12)**
5. **Aba Network**
6. **Procurar requisição de imagem**

Deve aparecer:
```
GET /api/uploads/2026/09/18/admin.png?token=abc123&expires=1730073600  200 OK ✅
```

---

### **3️⃣ Verificar no Inspector (HTML)**

Aba **Inspector/Elements** → Procurar tag `<img>`:

```html
<img src="http://localhost:8080/api/uploads/2026/09/18/admin.png?token=abc123&expires=1730073600" />
```

✅ **Deve conter `?token=...&expires=...`**

---

## ✅ **Resultado Esperado**

### **ANTES da correção:**
```
❌ GET /api/uploads/foto.png → 403 Forbidden
   (sem token)
```

### **DEPOIS da correção:**
```
✅ GET /api/uploads/foto.png?token=abc&expires=123 → 200 OK
   (com token)
```

---

## 📋 **Checklist de Validação**

- [ ] ✅ Frontend reiniciado
- [ ] ✅ Login funciona
- [ ] ✅ Foto de perfil carrega
- [ ] ✅ DevTools → Network mostra status 200 OK
- [ ] ✅ URL no Network contém `?token=...&expires=...`
- [ ] ✅ Tag `<img>` no HTML contém token

---

## 🚀 **Próximos Passos**

Se tudo funcionou em DEV:

1. ✅ Fazer commit das mudanças:
   ```bash
   git add .
   git commit -m "fix: preservar parâmetros de Signed URLs no frontend"
   git push
   ```

2. ✅ Deploy no Dockploy (produção)

3. ✅ Testar em produção

---

## 🔄 **Compatibilidade**

A correção é **100% compatível** com URLs antigas (sem assinatura):

- ✅ URLs novas (com `?token=...`) → Preserva token
- ✅ URLs antigas (sem token) → Continua funcionando como antes

---

## 💡 **Por Que Funcionou?**

### **Lógica da Correção:**

```typescript
// Detecção inteligente:
if (this.foto.includes('?token=') && this.foto.includes('&expires=')) {
  // É Signed URL → Preservar TUDO!
  return backendUrl + this.foto;
} else {
  // URL antiga → Aplicar lógica de normalização
  // (código antigo continua funcionando)
}
```

Isso garante:
- ✅ Novas URLs assinadas funcionam
- ✅ URLs antigas continuam funcionando
- ✅ Zero quebra de compatibilidade

---

**Correção implementada em:** 28/10/2026  
**Versão:** 1.0  
**Status:** ✅ **Pronto para testar**
