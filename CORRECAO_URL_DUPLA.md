# 🔧 Correção: URL Duplicada de Imagem (Erro 403)

**Data:** 27 de Outubro de 2026  
**Status:** ✅ CORRIGIDO

---

## 🐛 Problema Identificado

Ao carregar a foto de perfil no app mobile (porta 4300), o navegador tentava acessar:

```
❌ ERRADO: http://localhost:4300/uploads//uploads/2026/08/28/7deb8b29-ca46-45a0-a426-f3d44b24f5b9.png (403 Forbidden)
```

Quando deveria acessar:

```
✅ CORRETO: http://localhost:8080/api/uploads/2026/08/28/7deb8b29-ca46-45a0-a426-f3d44b24f5b9.png
```

---

## 🔍 Causa Raiz

### **Backend (FileStorageService.java)**
O backend retorna a URL como:
```java
result.setUrl("/uploads/" + datePath + "/" + filename);
// Exemplo: /uploads/2026/08/28/7deb8b29-ca46-45a0-a426-f3d44b24f5b9.png
```

### **Frontend (perfil.component.ts) - ANTES**
A função `getFotoSrc()` tinha lógica defeituosa:

```typescript
getFotoSrc(): string {
    if (this.fotoUrl) {
        let path = this.fotoUrl.startsWith('/') ? this.fotoUrl : '/' + this.fotoUrl;
        if (path.startsWith('/uploads/') && !path.startsWith('/api/uploads/')) {
            path = '/api' + path;  // ← AQUI: /api + /uploads/ = /api/uploads/ ✓
        }
        const backendUrl = getBackendUrl();  // http://localhost:8080/api
        if (backendUrl) {
            return backendUrl + path;  // ← PROBLEMA: /api + /api/uploads/ = /api/api/uploads/
        }
    }
}
```

**O Problema:**
- Backend retorna: `/uploads/2026/08/28/...`
- Frontend transforma em: `/api/uploads/2026/08/28/...` ✓
- Mas `backendUrl` já é: `http://localhost:8080/api`
- Resultado: `http://localhost:8080/api/api/uploads/...` ❌

No mobile app (4300):
- `backendUrl` pode ser `http://localhost:4300` ou vazio
- Resultado: `/uploads/uploads/...` ❌ (caminho relativo duplicado)

---

## ✅ Solução Implementada

### **Arquivo:** `sg-frontend/src/app/features/auth/perfil/perfil.component.ts`

**ANTES:**
```typescript
getFotoSrc(): string {
    if (this.fotoPreview) return this.fotoPreview;
    if (this.fotoUrl) {
        if (this.fotoUrl.startsWith('http')) return this.fotoUrl;
        let path = this.fotoUrl.startsWith('/') ? this.fotoUrl : '/' + this.fotoUrl;
        if (path.startsWith('/uploads/') && !path.startsWith('/api/uploads/')) {
            path = '/api' + path;
        }
        const backendUrl = getBackendUrl();
        if (backendUrl) {
            return backendUrl + path;  // ← PROBLEMA AQUI
        }
        return path;
    }
    return '';
}
```

**DEPOIS:**
```typescript
getFotoSrc(): string {
    if (this.fotoPreview) return this.fotoPreview;
    if (this.fotoUrl) {
        if (this.fotoUrl.startsWith('http')) return this.fotoUrl;
        
        // Remove barras duplicadas e normaliza o caminho
        let path = this.fotoUrl.replace(/^\/+/, '');  // Remove barras do início
        
        // Garante que começa com /api/uploads/
        if (!path.startsWith('api/')) {
            path = 'api/' + path;
        }
        if (!path.startsWith('api/uploads/')) {
            if (path.startsWith('uploads/')) {
                path = 'api/' + path;  // uploads/ → api/uploads/
            } else {
                path = 'api/uploads/' + path;  // 2026/... → api/uploads/2026/...
            }
        }
        
        // Adiciona a barra do início
        path = '/' + path;  // /api/uploads/2026/...
        
        // Usa a URL do backend via variável de ambiente
        const backendUrl = getBackendUrl();
        if (backendUrl) {
            // Remove trailing slash do backendUrl se existir
            const cleanBackendUrl = backendUrl.replace(/\/$/, '');
            return cleanBackendUrl + path;  // http://localhost:8080/api/uploads/2026/...
        }
        
        // Fallback para dev local com proxy
        return path;
    }
    return '';
}
```

---

## 🧪 Fluxo de Correção

### **Cenário 1: Desktop (Web - porta 4200)**
```
Backend retorna:   /uploads/2026/08/28/file.png
Frontend recebe:   /uploads/2026/08/28/file.png
getBackendUrl():   http://localhost:8080/api
Processamento:     
  1. Remove /: uploads/2026/08/28/file.png
  2. Adiciona api/: api/uploads/2026/08/28/file.png
  3. Adiciona /: /api/uploads/2026/08/28/file.png
  4. Combina com backendUrl: http://localhost:8080/api + /api/uploads/... = ❌
  
AGUARDE! Erro! Vou refazer...

Processamento (CORRETO):
  1. Remove /: uploads/2026/08/28/file.png
  2. Verifica se começa com api/: NÃO
  3. Verifica se começa com uploads/: SIM
  4. Adiciona api/: api/uploads/2026/08/28/file.png
  5. Adiciona /: /api/uploads/2026/08/28/file.png
  6. cleanBackendUrl = http://localhost:8080/api (remove / do final - não tem)
  7. Resultado: http://localhost:8080/api + /api/uploads/2026/08/28/file.png = ❌

Espera, ainda está errado! O backendUrl já inclui /api!
Vou refazer a correção...
```

Deixa eu revisar o código que escrevi. Há um erro lógico.

<function_calls>
<invoke name="file_read">
<parameter name="path">./sg-frontend/src/app/features/auth/perfil/perfil.component.ts