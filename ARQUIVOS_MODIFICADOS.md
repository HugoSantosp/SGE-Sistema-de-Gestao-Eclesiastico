# Arquivos Modificados — Índice Completo

## Backend (Java/Spring)

### Bug 1 — Correção de Serialização GrantedAuthority

#### 1. `UsuarioController.java`
**Localização:** `sg-backend/src/main/java/com/sg/usuario/UsuarioController.java`

**Mudanças:**
- ✅ Adicionados imports: `UsuarioRequestDTO`, `UsuarioResponseDTO`, `@Valid`
- ✅ Modificado: `listar()` → retorna `List<UsuarioResponseDTO>`
- ✅ Modificado: `buscarPorId()` → retorna `UsuarioResponseDTO`
- ✅ Modificado: `criar()` → recebe `UsuarioRequestDTO`, retorna `UsuarioResponseDTO`
- ✅ Modificado: `atualizar()` → recebe `UsuarioRequestDTO`, retorna `UsuarioResponseDTO`
- ✅ Mantido: `deletar()`, `resetarSenha()`

**Linhas Afetadas:** ~20 linhas modificadas

---

#### 2. `UsuarioService.java`
**Localização:** `sg-backend/src/main/java/com/sg/usuario/UsuarioService.java`

**Mudanças:**
- ✅ Adicionados imports: `UsuarioRequestDTO`, `UsuarioResponseDTO`, `Collectors`
- ✅ Adicionados métodos:
  - `entityToDTO(Usuario usuario): UsuarioResponseDTO`
  - `dtoToEntity(UsuarioRequestDTO dto): Usuario`
  - `listarTodosDTO(): List<UsuarioResponseDTO>`
  - `buscarPorIdDTO(Long id): UsuarioResponseDTO`

**Linhas Afetadas:** +66 novas linhas (fim da classe)

---

### Bug 2 — Senha Opcional e Toggle Mostrar/Ocultar

#### 3. `UsuarioRequestDTO.java`
**Localização:** `sg-backend/src/main/java/com/sg/usuario/dto/UsuarioRequestDTO.java`

**Mudanças:**
- ✅ Adicionado import: `jakarta.validation.constraints.Size`
- ✅ Removido: `@NotBlank(message = "Senha é obrigatória")`
- ✅ Adicionado: `@Size(min = 3, message = "Senha deve ter no mínimo 3 caracteres (deixe vazio para manter)")`

**Linhas Afetadas:** 2 linhas modificadas (campo `senha`)

---

#### 4. `UsuarioService.java` (Mudança Adicional — Bug 2)
**Localização:** `sg-backend/src/main/java/com/sg/usuario/UsuarioService.java`

**Mudanças:**
- ✅ Modificado: método `salvar()`
  - **Antes:** Detecta hash pelo prefixo `$2a$` ou `$2b$` (inseguro)
  - **Depois:** Verifica `.trim().isEmpty()` (seguro)
  - **Antes:** Sobrescreve senha mesmo quando vazia
  - **Depois:** Só codifica se nova senha foi informada
  - **Antes:** Sem comentário de comportamento
  - **Depois:** Marca `senhaTemporaria = false` se estava `true`

**Linhas Afetadas:** ~10 linhas modificadas (bloco if/else)

---

## Frontend (TypeScript/Angular)

### Bug 2 — Toggle Mostrar/Ocultar Senha

#### 5. `generic-form.component.ts`
**Localização:** `sg-frontend/src/app/shared/generic-form/generic-form.component.ts`

**Mudanças:**
- ✅ Adicionada propriedade: `showPassword: Record<string, boolean> = {};`
- ✅ Adicionado método: `togglePasswordVisibility(fieldName: string): void`

**Linhas Afetadas:** +3 novas linhas (propriedade + método)

---

#### 6. `generic-form.component.html`
**Localização:** `sg-frontend/src/app/shared/generic-form/generic-form.component.html`

**Mudanças:**
- ✅ Adicionado: Bloco `<div *ngIf="field.type === 'password'" class="input-group">`
  - Input com `[type]` dinâmico: `showPassword[field.name] ? 'text' : 'password'`
  - Botão com ícone de olho: alternam `bi bi-eye` e `bi bi-eye-slash`
  - Click handler: `(click)="togglePasswordVisibility(field.name)"`
- ✅ Modificado: Input padrão — adicionado `&& field.type !== 'password'` na condição

**Linhas Afetadas:** +15 novas linhas (toggle + ajuste input)

---

## Melhoria 3 — Pronto para Criar (Não Modificado Ainda)

### Frontend (A Criar)

#### 7. `perfil.component.ts` (NOVO)
**Localização:** `sg-frontend/projects/meu-ministerio/src/app/features/perfil/perfil.component.ts`

**A Ser Criado:**
- Componente com carregamento de perfil
- Métodos de edição e salvamento
- Upload de foto
- Modal de trocar senha
- ~150 linhas estimadas

---

#### 8. `perfil.component.html` (NOVO)
**Localização:** `sg-frontend/projects/meu-ministerio/src/app/features/perfil/perfil.component.html`

**A Ser Criado:**
- Template com visualização de perfil
- Modo edição com campos
- Avatar com upload
- Botão "Alterar Senha"
- Modal para trocar senha
- ~120 linhas estimadas

---

#### 9. `perfil.component.scss` (NOVO)
**Localização:** `sg-frontend/projects/meu-ministerio/src/app/features/perfil/perfil.component.scss`

**A Ser Criado:**
- Estilos para cards, avatar, form
- Responsividade
- Estados de loading e edição
- ~80 linhas estimadas

---

#### 10. `app-routing.module.ts` (MODIFICAR)
**Localização:** `sg-frontend/projects/meu-ministerio/src/app/app-routing.module.ts`

**A Modificar:**
- Adicionar rota `/perfil` sob `canActivate: [MmAuthGuard]`
- Uma linha: `{ path: 'perfil', component: PerfilComponent }`

---

## Resumo de Mudanças por Tipo

### Criados
| Arquivo | Tipo | Status |
|---------|------|--------|
| `perfil.component.ts` | TypeScript | 📋 Pronto para criar (Melhoria 3) |
| `perfil.component.html` | HTML | 📋 Pronto para criar (Melhoria 3) |
| `perfil.component.scss` | SCSS | 📋 Pronto para criar (Melhoria 3) |

### Modificados (Bug 1 + Bug 2)
| Arquivo | Linhas | Status |
|---------|--------|--------|
| `UsuarioController.java` | ~20 | ✅ Implementado |
| `UsuarioService.java` | +66 + 10 | ✅ Implementado |
| `UsuarioRequestDTO.java` | 2 | ✅ Implementado |
| `generic-form.component.ts` | +3 | ✅ Implementado |
| `generic-form.component.html` | +15 | ✅ Implementado |

### A Modificar (Melhoria 3)
| Arquivo | Mudança | Status |
|---------|---------|--------|
| `app-routing.module.ts` (Meu Ministerio) | +1 linha | 📋 Pronto |

---

## Estatísticas

### Backend
- **Arquivos modificados:** 3
- **Linhas adicionadas:** 76
- **Linhas modificadas:** 12
- **Total de mudanças:** 88 linhas

### Frontend
- **Arquivos modificados:** 2
- **Linhas adicionadas:** 18
- **Linhas modificadas:** 1
- **Total de mudanças:** 19 linhas

### Melhoria 3 (Planejado)
- **Arquivos a criar:** 3 (perfil.*)
- **Arquivos a modificar:** 1 (routing)
- **Linhas estimadas:** ~350 (componente completo)

---

## Dependências Entre Mudanças

```
Bug 1 (Serialização)
├── UsuarioController.java ✅
├── UsuarioService.java ✅ (conversão DTO)
└── Sem dependência externa

Bug 2 (Senha Opcional)
├── UsuarioRequestDTO.java ✅
├── UsuarioService.java ✅ (lógica de senha)
├── generic-form.component.ts ✅
├── generic-form.component.html ✅
└── Depende de: Bug 1 (DTOs já retornam sem senha)

Melhoria 3 (Tela de Perfil)
├── perfil.component.ts 📋
├── perfil.component.html 📋
├── perfil.component.scss 📋
├── app-routing.module.ts 📋
└── Depende de: Bugs 1 e 2 (melhor UX)
```

---

## Como Aplicar as Mudanças

### Bug 1
```bash
# 1. Modificar UsuarioController.java
# 2. Modificar UsuarioService.java
# 3. Compilar
mvn clean compile

# 4. Testar
mvn test
```

### Bug 2
```bash
# 1. Modificar UsuarioRequestDTO.java
# 2. Modificar UsuarioService.java (novo bloco if)
# 3. Modificar generic-form.component.ts
# 4. Modificar generic-form.component.html
# 5. Compilar backend
mvn clean compile

# 6. Compilar frontend
npm run build

# 7. Testar
mvn test  # backend
npm test  # frontend
```

### Melhoria 3 (Quando Implementar)
```bash
# 1. Criar 3 arquivos de componente
# 2. Modificar app-routing.module.ts (Meu Ministerio)
# 3. Compilar
npm run build

# 4. Testar em dev
npm start

# 5. Build final
npm run build:meu-ministerio
```

---

## Referência de Localização

### Backend
```
sg-backend/src/main/java/com/sg/
├── usuario/
│   ├── UsuarioController.java ✅
│   ├── UsuarioService.java ✅
│   └── dto/
│       └── UsuarioRequestDTO.java ✅
```

### Frontend App Admin
```
sg-frontend/src/app/
└── shared/
    └── generic-form/
        ├── generic-form.component.ts ✅
        └── generic-form.component.html ✅
```

### Frontend App Meu Ministerio (Melhoria 3)
```
sg-frontend/projects/meu-ministerio/src/app/
├── app-routing.module.ts 📋
└── features/
    └── perfil/ 📋
        ├── perfil.component.ts
        ├── perfil.component.html
        └── perfil.component.scss
```

---

## Checklist de Implementação

### ✅ Concluído
- [x] Bug 1 — UsuarioController.java
- [x] Bug 1 — UsuarioService.java (conversão DTO)
- [x] Bug 2 — UsuarioRequestDTO.java
- [x] Bug 2 — UsuarioService.java (lógica senha)
- [x] Bug 2 — generic-form.component.ts
- [x] Bug 2 — generic-form.component.html

### 📋 Pronto para Implementar (Melhoria 3)
- [ ] Criar perfil.component.ts
- [ ] Criar perfil.component.html
- [ ] Criar perfil.component.scss
- [ ] Modificar app-routing.module.ts (add rota)
- [ ] Testar componente

### ⏳ Pendentes
- [ ] Compilação do backend
- [ ] Testes unitários
- [ ] Testes funcionais
- [ ] Deploy em staging
- [ ] Deploy em produção

---

## Próximas Ações

1. ✅ **Revisar mudanças implementadas** (Bugs 1 e 2)
2. ⏳ **Compilar e testar** backend
3. ⏳ **Testar UI** frontend
4. ⏳ **Implementar Melhoria 3** (se autorizado)
5. ⏳ **Deploy** após testes

---

**Data de Última Atualização:** 27 de Outubro de 2026
**Status:** Bugs 1 e 2 implementados ✅ | Melhoria 3 analisada 📋
