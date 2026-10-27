# Análise Completa — Melhoria 3: Tela de Perfil para Usuários do Meu Ministério

## 1. Objetivo da Melhoria

Implementar uma tela de **Meu Perfil** no módulo "Meu Ministério" que permita:
- ✅ Usuários visualizarem seus dados pessoais
- ✅ Usuários atualizarem dados autorizados (nome, email, foto)
- ✅ Usuários alterarem sua própria senha
- ✅ Botão mostrar/ocultar senha para melhor UX
- ✅ Validações de segurança no backend
- ✅ Sem elevar privilégios

---

## 2. Investigação Técnica

### 2.1 Arquitetura Atual

**App Frontend:** 
- Dois apps separados:
  1. **SGE-Administração** (`/sg-frontend/src/app`) — Já tem `/perfil`
  2. **Meu Ministério** (`/sg-frontend/projects/meu-ministerio/src/app`) — Não tem perfil

**Backend Compartilhado:**
- Mesma API em `/auth` para ambos os apps
- Endpoints disponíveis:
  - `GET /auth/me` — Retorna dados do perfil
  - `PUT /auth/me` — Atualiza perfil
  - `PUT /auth/alterar-senha` — Altera senha (requer senha atual)

### 2.2 Análise do Backend

#### **Endpoint GET /auth/me**
```java
@GetMapping("/me")
@Operation(summary = "Meus dados", description = "Retorna os dados do perfil do usuário autenticado")
public ResponseEntity<PerfilResponseDTO> me(Authentication authentication) {
    return ResponseEntity.ok(authService.getCurrentUserProfile(authentication.getName()));
}
```
- ✅ Retorna `PerfilResponseDTO` (sem expor senha)
- ✅ Usa token JWT para identificar usuário
- ✅ Sem parâmetros de query (seguro)

#### **Endpoint PUT /auth/me**
```java
@PutMapping("/me")
@Operation(summary = "Atualizar perfil", description = "Atualiza nome, email e foto do usuário autenticado")
public ResponseEntity<PerfilResponseDTO> atualizarPerfil(
        @RequestBody @Valid AtualizarPerfilRequestDTO request,
        Authentication authentication) {
    return ResponseEntity.ok(authService.atualizarPerfil(request, authentication.getName()));
}
```
- ✅ Usa `authentication.getName()` (email do usuário logado)
- ✅ Não aceita ID como parâmetro (seguro)
- ✅ Valida com `@Valid`

#### **Endpoint PUT /auth/alterar-senha**
```java
@PutMapping("/alterar-senha")
@Operation(summary = "Alterar senha", description = "Altera a senha do usuário autenticado (requer senha atual)")
public ResponseEntity<Map<String, String>> alterarSenha(
        @RequestBody @Valid AlterarSenhaRequestDTO request,
        Authentication authentication) {
    authService.alterarSenha(request, authentication.getName());
    return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
}
```
- ✅ Requer senha atual (validação)
- ✅ Usa token JWT para identificar
- ✅ Não permite editar outros usuários

### 2.3 Análise do Frontend Meu Ministério

**Estrutura Atual:**
```
projects/meu-ministerio/src/app/features/
├── escala-detalhe/
├── escala-gerenciar/
├── escala-publico/
├── escalas/
├── login/
├── ministerio-lider/
├── ministerios/
└── trocar-senha/  ← Existe, mas apenas para trocar senha
```

**Roteamento Atual:**
```typescript
{ path: '', redirectTo: 'ministerios', pathMatch: 'full' },
{ path: 'ministerios', component: MinisteriosComponent },
{ path: 'ministerios/:id/gerenciar', component: MinisterioLiderComponent },
{ path: 'escalas', component: EscalasComponent },
{ path: 'escalas/:id', component: EscalaDetalheComponent },
{ path: 'escalas/:id/gerenciar', component: EscalaGerenciarComponent }
```

**Sem tela de perfil mapeada.** ❌

### 2.4 Componente Existente: TrocarSenhaComponent

**Localização:** `projects/meu-ministerio/src/app/features/trocar-senha/`

**Funcionalidade:**
- ✅ Pede senha atual
- ✅ Nova senha com confirmação
- ✅ Valida tamanho mínimo (6 caracteres)
- ✅ Chama `authService.alterarSenha()`
- ✅ Redireciona após sucesso

**Será Reusável:** Para incluir como modal dentro da tela de perfil

---

## 3. Requisitos Detalhados

### 3.1 Visualização do Perfil

**O que exibir:**
- ✅ Nome do usuário (ex: "João Silva")
- ✅ Email (ex: "joao@example.com")
- ✅ Nível de acesso (ex: "🧑 Membro", "🎤 Cantor")
- ✅ Documento/CPF (se disponível)
- ✅ Foto de perfil (avatar)
- ✅ Data de cadastro (opcional)

**Não exibir:**
- ❌ Senha (nunca)
- ❌ Hash ou tokens
- ❌ Dados administrativos (ID de pessoa, flags internas)

### 3.2 Edição de Dados

**Campos editáveis:**
- ✅ Nome (obrigatório)
- ✅ Email (obrigatório, validação)
- ✅ Foto de perfil (upload, opcional)

**Campos não editáveis:**
- ❌ Documento/CPF (vinculado ao cadastro de membro)
- ❌ Nível de acesso (definido pelo pastor)
- ❌ Email (considerar se deve permitir, sugiro não permitir sem verificação)

**Salvamento:**
- Upload de foto (se fornecida)
- Atualizar via `PUT /auth/me`
- Preservar dados não alterados

### 3.3 Alteração de Senha

**Requisitos:**
- ✅ Solicitar senha **atual** (validação de propriedade)
- ✅ Digitar **nova senha**
- ✅ **Confirmar** nova senha
- ✅ Botão mostrar/ocultar para ambas (UX melhor)
- ✅ Validação de tamanho mínimo (3 caracteres)
- ✅ Mensagem de sucesso/erro clara

**Implementação:**
- Reutilizar ou integrar `TrocarSenhaComponent`
- Ou criar modal dentro de `/perfil`

### 3.4 Botão Mostrar/Ocultar Senha

**Comportamento:**
- ✅ Ícone de olho ao lado de campos password
- ✅ Clicar alterna entre `type="password"` e `type="text"`
- ✅ Aplicado em:
  - Senha atual (ao alterar)
  - Nova senha (ao alterar)
  - Confirmação (ao alterar)

**Estados:**
- **Padrão:** Oculto (👁️ aberto) `type="password"`
- **Após clique:** Visível (👁️ fechado) `type="text"`

---

## 4. Arquitetura da Solução

### 4.1 Estrutura de Pastas Proposta

```
projects/meu-ministerio/src/app/features/perfil/
├── perfil.component.ts
├── perfil.component.html
├── perfil.component.scss
└── (reutiliza TrocarSenhaComponent via import)
```

### 4.2 Componente Perfil (novo)

**Responsabilidades:**
1. Carregar dados do usuário (`GET /auth/me`)
2. Renderizar modo "visualização"
3. Alternar para modo "edição"
4. Upload de foto
5. Atualizar perfil (`PUT /auth/me`)
6. Abrir/fechar modal de trocar senha

**Dados:**
```typescript
profile: PerfilResponseDTO | null = null;
editando = false;
nome = '';
email = '';
fotoUrl = '';
fotoFile: File | null = null;
fotoPreview: string | null = null;
loading = false;
uploading = false;
success = false;
error = '';
showAlterarSenha = false;  // Modal
```

**Métodos:**
- `carregarPerfil()` — GET /auth/me
- `iniciarEdicao()` — Ativa modo edição
- `cancelarEdicao()` — Cancela e reverte
- `onFotoSelected()` — Upload de foto
- `removerFoto()` — Remove seleção
- `salvar()` — PUT /auth/me + upload
- `abrirAlterarSenha()` — Abre modal
- `fecharAlterarSenha()` — Fecha modal

### 4.3 Roteamento

**Adicionar em `app-routing.module.ts` (Meu Ministério):**
```typescript
{
  path: '',
  canActivate: [MmAuthGuard],
  children: [
    // ... rotas existentes ...
    { path: 'perfil', component: PerfilComponent },  // ← NOVA
  ]
}
```

### 4.4 Integração com Componente TrocarSenha

**Opção 1 (Recomendada):** Importar como componente filho
```typescript
// em perfil.component.html
<app-trocar-senha 
  *ngIf="showAlterarSenha"
  (fechou)="fecharAlterarSenha(false)"
  (senhaAlterada)="fecharAlterarSenha(true)">
</app-trocar-senha>
```

**Opção 2:** Duplicar a lógica (não recomendado)

---

## 5. Segurança

### 5.1 Validações no Backend

- ✅ `authentication.getName()` — Usa token JWT, não parâmetro
- ✅ `@Valid` — Valida campos de entrada
- ✅ Alteração de senha — Requer senha atual
- ✅ Email validado com `@Email`
- ✅ Sem exposição de dados sensíveis

### 5.2 Validações no Frontend

- ✅ Campo obrigatório: Nome
- ✅ Campo obrigatório: Email
- ✅ Validação de email (regex ou HTML5)
- ✅ Botão "Salvar" desabilitado durante envio
- ✅ Confirmação de sair se houver mudanças

### 5.3 Proteção contra Abuso

- ✅ Senha atual obrigatória para trocar
- ✅ Limitar tentativas (backend)
- ✅ RateLimit em `/auth/alterar-senha`
- ✅ Logs de alteração

---

## 6. Arquivos a Criar/Modificar

### Backend — ❌ Sem mudanças necessárias
- Endpoints já existem
- Validações já presentes
- DTOs já adequados

### Frontend — ✅ 4 arquivos

| Arquivo | Ação | Descrição |
|---------|------|-----------|
| `perfil.component.ts` | **Criar** | Lógica do componente |
| `perfil.component.html` | **Criar** | Template |
| `perfil.component.scss` | **Criar** | Estilos |
| `app-routing.module.ts` | **Modificar** | Adicionar rota |

---

## 7. Impactos e Riscos

### Impactos Positivos ✅
- Usuário pode visualizar e editar seus dados
- Melhora UX do app Meu Ministério
- Reduz dependência de admin para alterações simples
- Segurança: backend valida propriedade via JWT

### Riscos Mitigados ⚠️

| Risco | Mitigação |
|-------|-----------|
| Usuário altera dados de outro | Backend usa `authentication.getName()` |
| Email duplicado | Validação no backend (unique constraint) |
| Foto muito grande | Validação de tamanho no frontend (5MB) |
| Senha fraca | Validação de tamanho mínimo |
| CSRF no alterar senha | CSRF protection (Spring Security) |

---

## 8. Plano de Testes

### Teste 1: Carregar Perfil
**Passos:** Acessar `/perfil`
**Esperado:** Dados do usuário carregados e exibidos

### Teste 2: Editar Nome
**Passos:** Clicar "Editar", alterar nome, salvar
**Esperado:** Nome atualizado, email não muda

### Teste 3: Editar Email
**Passos:** Clicar "Editar", alterar email, salvar
**Esperado:** Email atualizado validado

### Teste 4: Upload Foto
**Passos:** Clicar em foto, selecionar arquivo, salvar
**Esperado:** Foto enviada e exibida

### Teste 5: Trocar Senha
**Passos:** Clicar "Alterar Senha", preencher, confirmar
**Esperado:** Senha alterada, pode fazer login com nova

### Teste 6: Mostrar/Ocultar Senha
**Passos:** Digitar senha, clicar ícone de olho
**Esperado:** Alterna entre oculto e visível

### Teste 7: Validação Email Inválido
**Passos:** Tentar salvar com email inválido
**Esperado:** Erro de validação

### Teste 8: Cancelar Edição
**Passos:** Iniciar edição, cancelar
**Esperado:** Dados não mudam

---

## 9. Componentes Reutilizáveis

### Componentes do app Admin que Podem Servir de Referência

**`sg-frontend/src/app/features/auth/perfil/`**
- Excelente referência para layout e estilo
- Mesma estrutura de edição
- Mesmo componente trocar-senha

**Diferenças:**
- App Meu Ministério usa tema/componentes próprios (Voluts-style)
- Pode copiar lógica, adaptar visual

---

## 10. Cronograma Estimado

| Tarefa | Tempo | Dependências |
|--------|-------|--------------|
| Criar componente TypeScript | 30 min | - |
| Criar template HTML | 45 min | Template |
| Criar estilos SCSS | 30 min | HTML |
| Integrar TrocarSenha | 15 min | Componente TS |
| Atualizar roteamento | 5 min | - |
| Testes funcionais | 45 min | Componente completo |
| **TOTAL** | **2h 50min** | - |

---

## 11. Próximas Ações

✅ **Análise concluída.**

Aguardando autorização para:
1. Criar `perfil.component.ts`, `.html`, `.scss`
2. Atualizar roteamento em `app-routing.module.ts`
3. Implementar lógica de carregamento, edição e upload
4. Testar e validar

---

## Resumo Executivo

**Objetivo:** Tela "Meu Perfil" no app Meu Ministério

**Escopo:**
- Visualizar dados pessoais
- Editar nome, email, foto
- Alterar senha com validação
- Botão mostrar/ocultar senha

**Backend:** ✅ Pronto (endpoints `/auth/me`, `/auth/alterar-senha` já existem)
**Frontend:** 📝 Novo (3 novos arquivos, 1 modificação)
**Segurança:** ✅ Backend valida propriedade via JWT
**Tempo:** ~3 horas

**Status:** Pronto para implementação.
