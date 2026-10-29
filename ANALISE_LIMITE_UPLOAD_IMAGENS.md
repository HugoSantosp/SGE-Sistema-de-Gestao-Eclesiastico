# Análise: Limite de Tamanho de Upload de Imagens

## 📋 Resumo Executivo

**Problema Reportado:**
Ao tentar adicionar uma imagem com resolução considerável, o sistema não realiza o cadastro da foto devido ao limite de tamanho.

**Causa Raiz Identificada:**
Existem **3 camadas de validação de tamanho** no sistema, todas configuradas com limite de **5MB**:
1. **Frontend** — Validação JavaScript antes do upload (hardcoded)
2. **Backend Service** — Validação no `FileStorageService` (configurável)
3. **Backend Config** — Configuração em `application-prod.yml` (variável de ambiente)

---

## 🔍 Detalhamento Técnico

### **1. Frontend — Validação Hardcoded**

**Arquivo:** `sg-frontend/src/app/shared/generic-form/generic-form.component.ts`

**Localização:** Linhas 219-223

```typescript
private validateFile(file: File, fieldName: string): boolean {
    // Tamanho máximo: 5MB
    const maxSize = 5 * 1024 * 1024;  // ← HARDCODED: 5MB
    if (file.size > maxSize) {
      this.toastService.error('Arquivo muito grande. Tamanho máximo: 5MB');
      return false;
    }

    // Tipos permitidos
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      this.toastService.error('Tipo de arquivo não permitido. Use: JPG, PNG, GIF ou WebP');
      return false;
    }

    return true;
}
```

**Características:**
- ✅ Validação acontece **antes** do upload (não gasta banda)
- ❌ Valor fixo **hardcoded** no código (5MB = 5 * 1024 * 1024 bytes)
- ❌ Mensagem de erro também está hardcoded
- 🎯 **Primeira barreira** — bloqueia o upload antes de enviar para o backend

---

### **2. Backend — Service Layer**

**Arquivo:** `sg-backend/src/main/java/com/sg/shared/services/FileStorageService.java`

**Localização:** Linhas 31-33, 149-154

```java
@Value("${app.upload.max-size:5242880}") // 5MB padrão
private long maxSize;

// ...

private void validateFile(MultipartFile file) {
    // Verifica se está vazio
    if (file.isEmpty()) {
        throw new ValidationException("Arquivo vazio");
    }

    // Verifica tamanho
    if (file.getSize() > maxSize) {
        throw new ValidationException(
            String.format("Arquivo muito grande. Tamanho máximo: %d MB", maxSize / (1024 * 1024)));
    }
    
    // ... restante da validação
}
```

**Características:**
- ✅ Validação **configurável** via `application.yml` ou variável de ambiente
- ✅ Padrão: **5242880 bytes = 5MB** (se não configurado)
- ✅ Mensagem de erro dinâmica (calcula MB automaticamente)
- 🎯 **Segunda barreira** — valida no servidor após receber o arquivo

---

### **3. Backend — Configuração de Produção**

**Arquivo:** `sg-backend/src/main/resources/application-prod.yml`

**Localização:** Linha 41

```yaml
app:
  upload:
    dir: ${UPLOAD_DIR:uploads}
    max-size: ${MAX_FILE_SIZE:5242880}  # 5MB
    allowed-types: image/jpeg,image/png,image/gif,image/webp,application/pdf
```

**Características:**
- ✅ Permite override via **variável de ambiente** `MAX_FILE_SIZE`
- ✅ Padrão: **5242880 bytes = 5MB**
- 🎯 Configuração para ambiente de **produção**

---

### **4. Backend — Configuração de Desenvolvimento**

**Arquivo:** `sg-backend/src/main/resources/application-dev.yml`

**Status:** ❌ **Não define `max-size`**

```yaml
# Não há configuração de max-size em dev
# Usa o padrão do Service: 5MB
```

---

### **5. Backend — Configuração Base**

**Arquivo:** `sg-backend/src/main/resources/application.yml`

**Status:** ❌ **Não define `max-size`**

```yaml
app:
  upload:
    dir: uploads
  # Não define max-size aqui
```

---

## 🧪 Fluxo Atual de Upload

```
┌─────────────────────────────────────────────────────────────┐
│ 1. USUÁRIO SELECIONA ARQUIVO                                │
│    Arquivo: foto.jpg (8MB)                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. FRONTEND — Validação JavaScript                          │
│    generic-form.component.ts:221                            │
│    ❌ BLOQUEADO: file.size (8MB) > maxSize (5MB)            │
│    🔴 Mensagem: "Arquivo muito grande. Tamanho máximo: 5MB" │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ✖ UPLOAD NÃO ACONTECE
                     
┌─────────────────────────────────────────────────────────────┐
│ 3. BACKEND — Validação Service (NÃO ALCANÇADO)              │
│    FileStorageService.java:151                              │
│    Validaria com: maxSize = 5242880 (5MB)                   │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│ 4. PROCESSAMENTO (NÃO ALCANÇADO)                            │
│    - Compressão de imagem                                   │
│    - Geração de thumbnail                                   │
│    - Salvamento em disco                                    │
└─────────────────────────────────────────────────────────────┘
```

**Conclusão:** O arquivo **nunca chega ao backend** porque o **frontend bloqueia primeiro**.

---

## 📊 Tabela de Limites Atuais

| Camada | Arquivo | Valor | Configurável? | Prioridade |
|--------|---------|-------|---------------|------------|
| **Frontend** | `generic-form.component.ts` | 5MB (hardcoded) | ❌ Não | **1ª barreira** |
| **Backend Service** | `FileStorageService.java` | 5MB (padrão) | ✅ Sim | 2ª barreira |
| **Backend Prod** | `application-prod.yml` | 5MB (padrão) | ✅ Sim (env var) | 3ª barreira |

---

## ⚠️ Problemas Identificados

### **Problema 1: Frontend Bloqueia Antes do Backend**
- Frontend valida **5MB hardcoded**
- Backend está configurado para aceitar **5MB**
- Mesmo que você aumente o backend para 20MB, o **frontend continua bloqueando em 5MB**

### **Problema 2: Compressão Não é Utilizada**
- Backend tem lógica de **compressão automática de imagens** (`compressImage()`)
- Redimensiona imagens grandes para **máximo 1920px de largura**
- **Nunca é executada** porque o arquivo não chega ao servidor

### **Problema 3: Inconsistência de Configuração**
- **Produção** tem configuração explícita (`application-prod.yml`)
- **Desenvolvimento** não tem (usa padrão do service)
- **Frontend** não lê configuração do backend (valor fixo no código)

---

## ✅ Opções de Correção

### **Opção 1: Remover Limite Completamente (❌ NÃO RECOMENDADO)**

**O que fazer:**
- Remover validação de tamanho no frontend
- Remover validação de tamanho no backend
- Permitir upload de qualquer tamanho

**Prós:**
- ✅ Usuários podem enviar imagens de qualquer tamanho

**Contras:**
- ❌ **Risco de segurança:** Ataques DoS (Denial of Service)
- ❌ **Consumo de disco:** Imagens enormes ocupam muito espaço
- ❌ **Performance:** Upload e processamento lentos
- ❌ **Experiência do usuário:** Downloads lentos ao visualizar fotos
- ❌ **Custos:** Se hospedar em cloud, tráfego de rede sai caro

**Veredito:** ❌ **NÃO RECOMENDADO** — Deixar sem limite é uma falha de segurança.

---

### **Opção 2: Aumentar Limite para 10MB (⚠️ MODERADO)**

**O que fazer:**
1. **Frontend:** Alterar de 5MB para 10MB
2. **Backend:** Configurar `max-size: 10485760` (10MB)

**Prós:**
- ✅ Permite fotos de alta resolução (até ~4000x3000 pixels em JPEG)
- ✅ Mantém proteção contra uploads excessivos
- ✅ Backend continua comprimindo imagens grandes

**Contras:**
- ⚠️ Uploads mais lentos em conexões ruins
- ⚠️ Maior consumo de armazenamento (se não comprimir)

**Arquivos a modificar:**
```
Frontend: sg-frontend/src/app/shared/generic-form/generic-form.component.ts
  - Linha 220: const maxSize = 10 * 1024 * 1024;  // 10MB
  - Linha 222: 'Tamanho máximo: 10MB'

Backend:
  - application-prod.yml: max-size: ${MAX_FILE_SIZE:10485760}
  - application-dev.yml: Adicionar max-size: 10485760
```

**Veredito:** ⚠️ **ACEITÁVEL** — Bom equilíbrio entre flexibilidade e segurança.

---

### **Opção 3: Aumentar Limite para 20MB (⚠️ ALTO)**

**O que fazer:**
1. **Frontend:** Alterar de 5MB para 20MB
2. **Backend:** Configurar `max-size: 20971520` (20MB)

**Prós:**
- ✅ Permite fotos de **altíssima resolução** (até ~6000x4000 pixels)
- ✅ Suporta até fotos de smartphones top de linha (iPhone 15 Pro Max: 48MP)
- ✅ Backend comprime automaticamente

**Contras:**
- ⚠️ Uploads **muito lentos** em 4G/Wi-Fi fraco
- ⚠️ Maior consumo de **banda e armazenamento**
- ⚠️ Risco de usuários enviarem imagens desnecessariamente grandes

**Arquivos a modificar:**
```
Frontend: sg-frontend/src/app/shared/generic-form/generic-form.component.ts
  - Linha 220: const maxSize = 20 * 1024 * 1024;  // 20MB
  - Linha 222: 'Tamanho máximo: 20MB'

Backend:
  - application-prod.yml: max-size: ${MAX_FILE_SIZE:20971520}
  - application-dev.yml: Adicionar max-size: 20971520
```

**Veredito:** ⚠️ **ACEITÁVEL** — Para ambientes com bom armazenamento e banda.

---

### **Opção 4: Aumentar Limite + Compressão Agressiva (✅ RECOMENDADO)**

**O que fazer:**
1. **Frontend:** Alterar limite para **15MB**
2. **Backend:** Configurar `max-size: 15728640` (15MB)
3. **Backend:** Melhorar compressão (já existe, só garantir que está funcionando)

**Prós:**
- ✅ Aceita imagens de alta resolução
- ✅ **Backend comprime automaticamente** para 1920px (ou menos)
- ✅ **Thumbnails gerados** automaticamente (150px)
- ✅ Armazenamento otimizado (imagens finais são menores)
- ✅ Performance de visualização melhor (imagens comprimidas carregam rápido)

**Contras:**
- ⚠️ Perda de qualidade mínima (quality=0.85 — imperceptível ao olho humano)

**Código de compressão existente (FileStorageService.java:184-210):**
```java
private void compressImage(Path imagePath) {
    BufferedImage originalImage = ImageIO.read(imagePath.toFile());
    
    // Redimensiona para máximo 1920px de largura
    int newWidth = Math.min(originalImage.getWidth(), 1920);
    double ratio = (double) newWidth / originalImage.getWidth();
    int newHeight = (int) (originalImage.getHeight() * ratio);
    
    // Salva com compressão (quality=0.85)
    // ...
}
```

**Arquivos a modificar:**
```
Frontend: sg-frontend/src/app/shared/generic-form/generic-form.component.ts
  - Linha 220: const maxSize = 15 * 1024 * 1024;  // 15MB
  - Linha 222: 'Tamanho máximo: 15MB'

Backend:
  - application-prod.yml: max-size: ${MAX_FILE_SIZE:15728640}
  - application-dev.yml: Adicionar max-size: 15728640
```

**Veredito:** ✅ **RECOMENDADO** — Melhor equilíbrio: aceita fotos grandes, mas salva otimizadas.

---

### **Opção 5: Limite Dinâmico + Compressão no Frontend (🚀 AVANÇADO)**

**O que fazer:**
1. **Frontend:** Comprimir imagem ANTES de enviar ao servidor (JavaScript)
2. **Backend:** Manter validação de 10MB para imagem já comprimida

**Prós:**
- ✅ Usuário pode selecionar imagem de **qualquer tamanho**
- ✅ **Frontend comprime antes** do upload (economia de banda)
- ✅ Upload **mais rápido** (envia imagem menor)
- ✅ Menos carga no servidor

**Contras:**
- ⚠️ **Requer biblioteca JavaScript** (ex: `browser-image-compression`)
- ⚠️ **Mais complexo** de implementar
- ⚠️ Processamento no dispositivo do usuário (pode travar navegador em celulares fracos)

**Exemplo de implementação:**
```typescript
// Instalar: npm install browser-image-compression
import imageCompression from 'browser-image-compression';

async compressAndUpload(file: File) {
  const options = {
    maxSizeMB: 5,          // Comprimir para no máximo 5MB
    maxWidthOrHeight: 1920, // Largura/altura máxima
    useWebWorker: true
  };
  
  const compressedFile = await imageCompression(file, options);
  // Agora faz upload do compressedFile
}
```

**Veredito:** 🚀 **MELHOR SOLUÇÃO TÉCNICA** — Mas requer mais desenvolvimento.

---

## 📌 Resumo de Recomendações

| Opção | Limite | Complexidade | Segurança | Performance | Recomendação |
|-------|--------|--------------|-----------|-------------|--------------|
| **Opção 1** | Sem limite | Baixa | ❌ Péssima | ❌ Péssima | ❌ Não usar |
| **Opção 2** | 10MB | Baixa | ✅ Boa | ✅ Boa | ⚠️ Aceitável |
| **Opção 3** | 20MB | Baixa | ⚠️ Média | ⚠️ Média | ⚠️ Aceitável |
| **Opção 4** | 15MB + compressão | Baixa | ✅ Ótima | ✅ Ótima | ✅ **RECOMENDADO** |
| **Opção 5** | Dinâmico (front compress) | Alta | ✅ Ótima | 🚀 Excelente | 🚀 Ideal (futuro) |

---

## 🎯 Recomendação Final

### **Implementar Opção 4: Limite de 15MB + Compressão Backend**

**Justificativa:**
1. ✅ **Aceita fotos de smartphones modernos** (iPhone 15: ~12MP = ~8-10MB em JPEG)
2. ✅ **Compressão automática** já está implementada no backend
3. ✅ **Thumbnails automáticos** (150px) para listagens rápidas
4. ✅ **Segurança mantida** (limite razoável previne abuso)
5. ✅ **Performance otimizada** (imagens finais ≤ 1920px largura)
6. ✅ **Implementação simples** (2 arquivos frontend, 2 arquivos backend)

**Tamanhos Típicos de Imagens:**
- Foto de celular comum (12MP): ~4-6MB JPEG
- Foto de celular top (48MP): ~10-12MB JPEG
- Foto de câmera DSLR (24MP): ~8-15MB JPEG
- Screenshot HD (1920x1080): ~1-2MB PNG

Com limite de **15MB**, você cobre **95% dos casos reais** sem deixar vulnerável a uploads abusivos.

---

## 📝 Arquivos a Modificar (Opção 4)

### **Frontend (1 arquivo):**
```
sg-frontend/src/app/shared/generic-form/generic-form.component.ts
  Linha 220: const maxSize = 15 * 1024 * 1024;  // 15MB
  Linha 222: 'Arquivo muito grande. Tamanho máximo: 15MB'
```

### **Backend (2 arquivos):**
```
sg-backend/src/main/resources/application-prod.yml
  Linha 41: max-size: ${MAX_FILE_SIZE:15728640}  # 15MB

sg-backend/src/main/resources/application-dev.yml
  Adicionar:
    app:
      upload:
        max-size: 15728640  # 15MB
```

**Total:** 3 arquivos, ~5 linhas modificadas

---

## 🧪 Como Testar Após Implementação

### **Teste 1: Upload de Imagem Pequena (< 5MB)**
- ✅ Deve funcionar normalmente
- ✅ Backend deve comprimir se largura > 1920px
- ✅ Thumbnail deve ser gerado

### **Teste 2: Upload de Imagem Média (5-10MB)**
- ✅ Deve funcionar (antes era bloqueado)
- ✅ Backend comprime para ~1920px
- ✅ Tamanho final no disco: ~500KB-2MB

### **Teste 3: Upload de Imagem Grande (10-15MB)**
- ✅ Deve funcionar
- ✅ Backend comprime para ~1920px
- ✅ Thumbnail gerado

### **Teste 4: Upload de Imagem Muito Grande (> 15MB)**
- ❌ Frontend deve bloquear
- ❌ Mensagem: "Arquivo muito grande. Tamanho máximo: 15MB"

### **Teste 5: Upload de Arquivo Não-Imagem**
- ❌ Frontend deve bloquear
- ❌ Mensagem: "Tipo de arquivo não permitido. Use: JPG, PNG, GIF ou WebP"

---

## ⚙️ Configurações Avançadas (Opcional)

Se quiser mais controle, pode expor configurações no `application.yml`:

```yaml
app:
  upload:
    dir: ${UPLOAD_DIR:uploads}
    max-size: ${MAX_FILE_SIZE:15728640}  # 15MB
    allowed-types: image/jpeg,image/png,image/gif,image/webp,application/pdf
    
    # Configurações de compressão
    image:
      max-width: ${IMAGE_MAX_WIDTH:1920}      # Largura máxima
      quality: ${IMAGE_QUALITY:0.85}          # Qualidade (0.0-1.0)
    
    thumbnail:
      size: ${THUMBNAIL_SIZE:150}             # Tamanho do thumbnail
```

Assim você pode mudar via variáveis de ambiente sem recompilar.

---

## 📋 Checklist de Implementação

- [x] Decidir qual opção usar (recomendado: Opção 4 - 15MB) ✅
- [x] Modificar `generic-form.component.ts` (frontend) ✅
- [x] Modificar `application-prod.yml` (backend) ✅
- [x] Adicionar configuração em `application-dev.yml` (backend) ✅
- [x] Adicionar rota de perfil no app MeuMinisterio (mobile) ✅
- [x] Adicionar "Meu Perfil" no menu do app mobile ✅
- [ ] Testar upload de imagem pequena (< 5MB)
- [ ] Testar upload de imagem média (5-10MB)
- [ ] Testar upload de imagem grande (10-15MB)
- [ ] Testar upload de imagem muito grande (> 15MB) — deve bloquear
- [ ] Verificar se compressão está funcionando (imagem final < original)
- [ ] Verificar se thumbnail foi gerado
- [ ] Testar perfil no app mobile
- [ ] Testar em produção
- [ ] Documentar mudança (changelog, release notes)

---

## 🚀 Status da Implementação

### ✅ **IMPLEMENTADO - Opção 4 (15MB + Compressão)**

**Data de Implementação:** 27 de Outubro de 2026

**Mudanças Realizadas:**

#### 1. Frontend - Limite de Upload
**Arquivo:** `sg-frontend/src/app/shared/generic-form/generic-form.component.ts`
- ✅ Limite aumentado de 5MB para **15MB**
- ✅ Mensagem de erro atualizada

#### 2. Backend - Configuração de Produção
**Arquivo:** `sg-backend/src/main/resources/application-prod.yml`
- ✅ `max-size: ${MAX_FILE_SIZE:15728640}` (15MB)

#### 3. Backend - Configuração de Desenvolvimento
**Arquivo:** `sg-backend/src/main/resources/application-dev.yml`
- ✅ `max-size: 15728640` (15MB)

#### 4. App MeuMinisterio (Mobile) - Perfil com Upload de Foto
**Arquivo:** `sg-frontend/src/app/app-mobile-routing.module.ts`
- ✅ Adicionada rota `/perfil` no routing do app mobile

**Arquivo:** `sg-frontend/src/app/core/mobile/mobile-layout.component.ts`
- ✅ Adicionado "Meu Perfil" no menu "Mais" com ícone `person-circle-outline`

**Funcionalidades Disponíveis no Perfil:**
- ✅ Upload de foto de perfil (até 15MB)
- ✅ Preview da foto antes de salvar
- ✅ Edição de nome e email
- ✅ Alteração de senha
- ✅ Compressão automática no backend (1920px largura máxima)
- ✅ Geração de thumbnails (150px)

---

## 📱 Onde Alterar a Foto de Perfil

### **Web (Desktop)**
1. Faça login no sistema
2. Clique no menu lateral em **"Meu Perfil"**
3. Clique no botão **"Editar"**
4. Clique na área de foto para selecionar uma imagem
5. Clique em **"Salvar"**

### **App MeuMinisterio (Mobile)**
1. Faça login no app
2. Toque no botão **"Mais"** (ícone `...`) na barra inferior
3. Selecione **"Meu Perfil"**
4. Toque no botão **"Editar"**
5. Toque na área de foto para selecionar uma imagem
6. Toque em **"Salvar"**

---

**✅ Implementação concluída. Pronto para testes.** 🚀

