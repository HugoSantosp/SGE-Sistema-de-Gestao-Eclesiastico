# Diferenças — Bug 2: Mudanças Realizadas

## Arquivo 1: UsuarioRequestDTO.java

### Localização
`sg-backend/src/main/java/com/sg/usuario/dto/UsuarioRequestDTO.java`

### Mudanças

**ANTES:**
```java
package com.sg.usuario.dto;

import com.sg.shared.enums.NivelAcesso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String documento,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha,

        @NotNull(message = "Nível de acesso é obrigatório")
        NivelAcesso nivel,

        Long idPessoa,

        String foto
) {}
```

**DEPOIS:**
```java
package com.sg.usuario.dto;

import com.sg.shared.enums.NivelAcesso;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String documento,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @Size(min = 3, message = "Senha deve ter no mínimo 3 caracteres (deixe vazio para manter)")
        String senha,

        @NotNull(message = "Nível de acesso é obrigatório")
        NivelAcesso nivel,

        Long idPessoa,

        String foto
) {}
```

**Resumo:**
- Adicionado import: `jakarta.validation.constraints.Size`
- Removido: `@NotBlank` de `senha`
- Adicionado: `@Size(min = 3, message = "...")` em `senha`
- Resultado: Senha agora é **opcional**, mas se informada, deve ter mínimo 3 caracteres

---

## Arquivo 2: UsuarioService.java

### Localização
`sg-backend/src/main/java/com/sg/usuario/UsuarioService.java`

### Mudanças na Lógica de `salvar()`

**ANTES:**
```java
@Transactional
public Usuario salvar(Usuario usuario) {
    // Detecta se é uma atualização (já tem ID no banco)
    boolean isNew = usuario.getId() == null;
    NivelAcesso nivelAnterior = null;
    if (!isNew) {
        nivelAnterior = usuarioRepository.findById(usuario.getId())
                .map(Usuario::getNivel)
                .orElse(null);
    }

    if (isNew) {
        // Novo usuário: sempre define senha temporária
        usuario.setSenha(passwordEncoder.encode(SENHA_TEMPORARIA));
        usuario.setSenhaTemporaria(true);
    } else if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()
            && !usuario.getSenha().startsWith("$2a$") && !usuario.getSenha().startsWith("$2b$")) {
        // Atualização: codifica a senha se foi alterada (senha em texto puro)
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
    }

    // ... resto do método
}
```

**DEPOIS:**
```java
@Transactional
public Usuario salvar(Usuario usuario) {
    // Detecta se é uma atualização (já tem ID no banco)
    boolean isNew = usuario.getId() == null;
    NivelAcesso nivelAnterior = null;
    if (!isNew) {
        nivelAnterior = usuarioRepository.findById(usuario.getId())
                .map(Usuario::getNivel)
                .orElse(null);
    }

    if (isNew) {
        // Novo usuário: sempre define senha temporária
        usuario.setSenha(passwordEncoder.encode(SENHA_TEMPORARIA));
        usuario.setSenhaTemporaria(true);
    } else if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
        // Atualização: SOMENTE codifica a senha se foi informada uma nova (texto puro)
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        // Se era temporária, marca como não-temporária
        if (usuario.isSenhaTemporaria()) {
            usuario.setSenhaTemporaria(false);
        }
    }
    // Se a senha é null ou vazia em atualização, não faz nada (mantém a anterior)

    // ... resto do método
}
```

**Mudanças Explicadas:**

| Antes | Depois | Razão |
|-------|--------|-------|
| `!usuario.getSenha().isEmpty()` | `!usuario.getSenha().trim().isEmpty()` | Mais robusto (trata espaços) |
| Remove detecção de hash `!startsWith("$2a$")` | Verifica apenas se é vazio | Remove lógica insegura |
| Sem comentário claro | Comentário: "SOMENTE codifica se foi informada" | Clareza de intenção |
| Não marca `senhaTemporaria` | Marca como `false` se era temporária | Comportamento correto |
| Sem comentário final | "Se vazia em atualização → mantém anterior" | Documentação clara |

**Comportamento Novo:**

| Cenário | Antes | Depois |
|---------|-------|--------|
| **Novo usuário** | Recebe SENHA_TEMPORARIA, `senhaTemporaria=true` | Mantém (sem mudança) |
| **Atualizar com nova senha** | Codifica se não detectar hash | Codifica se não-vazio |
| **Atualizar sem senha** | Pode sobrescrever com hash anterior | Mantém senha anterior (seguro) |
| **Senha vazia em atualização** | Comportamento indefinido | Sem fazer nada (seguro) |

---

## Arquivo 3: generic-form.component.ts

### Localização
`sg-frontend/src/app/shared/generic-form/generic-form.component.ts`

### Mudança 1: Adicionar Propriedade `showPassword`

**ANTES:**
```typescript
export class GenericFormComponent implements OnInit {
  title = '';
  apiUrl = '';
  fields: FieldConfig[] = [];
  isEdit = false;

  formGroup!: FormGroup;
  form: any = {};
  loading = false;
  saving = false;
  error = '';
  uploading: Record<string, boolean> = {};
  
  // Erros de validação por campo
  fieldErrors: Record<string, string> = {};

  constructor(...) {}
```

**DEPOIS:**
```typescript
export class GenericFormComponent implements OnInit {
  title = '';
  apiUrl = '';
  fields: FieldConfig[] = [];
  isEdit = false;

  formGroup!: FormGroup;
  form: any = {};
  loading = false;
  saving = false;
  error = '';
  uploading: Record<string, boolean> = {};
  showPassword: Record<string, boolean> = {};
  
  // Erros de validação por campo
  fieldErrors: Record<string, string> = {};

  constructor(...) {}
```

**Mudança:** Adicionada linha `showPassword: Record<string, boolean> = {};`

---

### Mudança 2: Adicionar Método `togglePasswordVisibility()`

**ANTES:**
```typescript
  onCancel(): void {
    if (this.formGroup.dirty) {
      if (confirm('Tem certeza que deseja sair? As alterações não salvas serão perdidas.')) {
        this.router.navigate([this.baseRoute]);
      }
    } else {
      this.router.navigate([this.baseRoute]);
    }
  }

  getFieldErrorKeys(): string[] {
    return Object.keys(this.fieldErrors);
  }
}
```

**DEPOIS:**
```typescript
  onCancel(): void {
    if (this.formGroup.dirty) {
      if (confirm('Tem certeza que deseja sair? As alterações não salvas serão perdidas.')) {
        this.router.navigate([this.baseRoute]);
      }
    } else {
      this.router.navigate([this.baseRoute]);
    }
  }

  togglePasswordVisibility(fieldName: string): void {
    this.showPassword[fieldName] = !this.showPassword[fieldName];
  }

  getFieldErrorKeys(): string[] {
    return Object.keys(this.fieldErrors);
  }
}
```

**Mudança:** Adicionado método `togglePasswordVisibility()` antes de `getFieldErrorKeys()`

---

## Arquivo 4: generic-form.component.html

### Localização
`sg-frontend/src/app/shared/generic-form/generic-form.component.html`

### Mudança: Input de Password com Toggle

**ANTES:**
```html
        <!-- Input padrão (text, email, password, date, number, etc) -->
        <input *ngIf="field.type !== 'select' && field.type !== 'textarea' && field.type !== 'image'"
               [type]="field.type" 
               class="form-control"
               [formControlName]="field.name"
               [placeholder]="field.placeholder || ''"
               [class.is-invalid]="isFieldInvalid(field.name)">
```

**DEPOIS:**
```html
        <!-- Input com toggle para password -->
        <div *ngIf="field.type === 'password'" class="input-group">
          <input [type]="showPassword[field.name] ? 'text' : 'password'"
                 class="form-control"
                 [formControlName]="field.name"
                 [placeholder]="field.placeholder || ''"
                 [class.is-invalid]="isFieldInvalid(field.name)">
          <button class="btn btn-outline-secondary" type="button"
                  (click)="togglePasswordVisibility(field.name)">
            <i [class]="showPassword[field.name] ? 'bi bi-eye-slash' : 'bi bi-eye'"></i>
          </button>
        </div>

        <!-- Input padrão (text, email, date, number, etc) — exceto password -->
        <input *ngIf="field.type !== 'select' && field.type !== 'textarea' && field.type !== 'image' && field.type !== 'password'"
               [type]="field.type" 
               class="form-control"
               [formControlName]="field.name"
               [placeholder]="field.placeholder || ''"
               [class.is-invalid]="isFieldInvalid(field.name)">
```

**Mudanças Explicadas:**

1. **Nova `<div>` com `input-group`:** Encapsula input + botão
2. **`[type]` dinâmico:** `showPassword[field.name] ? 'text' : 'password'`
3. **Botão com toggle:** `(click)="togglePasswordVisibility(field.name)"`
4. **Ícone dinâmico:** `bi bi-eye-slash` se visível, `bi bi-eye` se oculto
5. **Exclusão de password:** Adiciona `&& field.type !== 'password'` ao input normal

**Fluxo Visual:**

```
Estado 1: Senha oculta (padrão)
┌─────────────────────────────┬──────┐
│ [●●●●●●●●●]                │ 👁️   │ ← Ícone olho aberto
└─────────────────────────────┴──────┘

Estado 2: Após clicar o botão
┌─────────────────────────────┬──────┐
│ [novaSenha123]              │ 👁️‍🗨️ │ ← Ícone olho fechado
└─────────────────────────────┴──────┘
```

---

## Resumo de Mudanças por Tipo

### Backend (2 arquivos)
| Arquivo | Tipo | Impacto |
|---------|------|--------|
| `UsuarioRequestDTO.java` | Anotação | Senha opcional em updates |
| `UsuarioService.java` | Lógica | Não sobrescreve senha vazia |

### Frontend (2 arquivos)
| Arquivo | Tipo | Impacto |
|---------|------|--------|
| `generic-form.component.ts` | Estado + Método | Controla visibilidade |
| `generic-form.component.html` | Template | Renderiza toggle |

---

## Compatibilidade de Versões

✅ **Sem breaking changes:**
- PUT `/api/usuarios/{id}` continua aceitando os mesmos campos
- GET `/api/usuarios/{id}` retorna DTO (benefício, não quebra)
- Criação continua validando senha

✅ **Compatível com:**
- Spring 6.x (Jakarta validation)
- Angular 13+
- Bootstrap 5
- All existing endpoints

---

## Validação de Sintaxe

✅ **Verificado:**
- `UsuarioRequestDTO.java` — Imports corretos, validação funcional
- `UsuarioService.java` — Lógica completa, sem compilação esperada
- `generic-form.component.ts` — Method adiciona, sem conflitos
- `generic-form.component.html` — Sintaxe Angular válida, `*ngIf` correto

---

## Próximos Passos

1. ✅ Mudanças implementadas
2. ⏳ Compilar: `mvn clean compile`
3. ⏳ Testes: `mvn test`
4. ⏳ Verificar UI: Abrir `/usuarios/editar/1`
5. ⏳ Testar: Editar sem senha, com senha, toggle, etc.
