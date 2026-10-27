# Análise Completa — Bug 2: Senha exibindo caracteres na tela "Editar Usuário"

## 1. Investigação do Comportamento

**Problema Relatado:** Ao acessar a tela de **Editar Usuário** (módulo administrativo), o campo de senha exibe uma sequência muito longa de caracteres, correspondente ao hash da senha armazenada no banco de dados.

**Localização:** Rota `/usuarios/editar/:id` — utiliza o componente `GenericFormComponent` para renderizar o formulário dinâmico.

---

## 2. Análise Técnica — Causa Raiz Confirmada

### 2.1 Fluxo de Carregamento de Dados para Edição

**Frontend (GenericFormComponent.ts):**
```typescript
if (this.isEdit) {
  this.loading = true;
  const id = this.route.snapshot.params['id'];
  this.http.get<any>(`${this.apiUrl}/${id}`).subscribe({
    next: (d) => { 
      this.formGroup.patchValue(d);  // ← Preenche o formulário com TODOS os campos retornados
      this.form = d;
      this.loading = false; 
    },
    ...
  });
}
```

**Resultado:** Qualquer campo retornado no JSON é automaticamente preenchido no formulário.

### 2.2 Configuração do Campo de Senha

**Frontend (usuarios.module.ts - rota de edição):**
```typescript
fields: [
  { name: 'nome', label: 'Nome', type: 'text', required: true },
  { name: 'email', label: 'Email', type: 'email', required: true },
  { name: 'senha', label: 'Senha (deixe vazio para manter)', type: 'password' },  // ← Campo de senha
  { name: 'documento', label: 'Documento...', type: 'text', required: true },
  { name: 'nivel', label: 'Nível de Acesso', type: 'select', required: true, ... },
  { name: 'foto', label: 'Foto de Perfil', type: 'image', ... }
]
```

**Intenção:** O label diz "deixe vazio para manter", indicando que a senha é opcional.

### 2.3 Problema: API Retorna Senha Codificada

**Antes da correção do Bug 1:**
- Endpoint `GET /api/usuarios/{id}` retornava a entidade `Usuario`
- A entidade `Usuario` incluía o campo `senha` com seu valor codificado (hash)
- Exemplo: `"senha": "$2b$10$NWpW7GJlxxx...xxx"` (60+ caracteres)

**Resultado no Frontend:**
```json
{
  "id": 1,
  "nome": "João",
  "email": "joao@example.com",
  "senha": "$2b$10$NWpW7GJlxxx...xxx",  // ← Hash aparecia no campo
  "documento": "12345678900",
  "nivel": "MEMBRO",
  "foto": null
}
```

O `patchValue()` preencheria o campo de senha com o hash.

### 2.4 Problema no Backend: UsuarioRequestDTO

```java
public record UsuarioRequestDTO(
    @NotBlank(message = "Senha é obrigatória")      // ← SEMPRE OBRIGATÓRIA
    String senha,
    ...
)
```

**Consequências:**
- Ao editar um usuário, a senha é **sempre obrigatória**
- Não há forma de atualizar apenas nome/email sem alterar a senha
- Contradiz o label do frontend que diz "deixe vazio para manter"

### 2.5 Fluxo de Atualização Atual

```typescript
// Frontend: envia o objeto completo
this.http.put(`/api/usuarios/${id}`, this.form, ...)

// Backend: recebe UsuarioRequestDTO
// UsuarioService.salvar():
if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()
    && !usuario.getSenha().startsWith("$2a$") && !usuario.getSenha().startsWith("$2b$")) {
    usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
}
```

**Problema:** Detecta se a senha é um hash (começando com `$2a$` ou `$2b$`), mas isso é:
- ❌ Inconfiável (hash pode estar em qualquer formato)
- ❌ Inseguro (expõe a lógica de codificação)
- ❌ Não segue a intenção do frontend ("deixe vazio")

---

## 3. Impacto da Correção Bug 1 Neste Cenário

**Com a correção do Bug 1:**
- Endpoint `GET /api/usuarios/{id}` agora retorna `UsuarioResponseDTO` 
- `UsuarioResponseDTO` **não inclui o campo `senha`**

**Resultado:**
```json
{
  "id": 1,
  "nome": "João",
  "email": "joao@example.com",
  "documento": "12345678900",
  "nivel": "MEMBRO",
  "foto": null
  // ← Sem campo 'senha'!
}
```

**Benefício:** ✅ O campo de senha no formulário **já não será preenchido** com o hash!

**Problema Restante:** ❌ O backend ainda rejeita PUT sem senha (UsuarioRequestDTO requer senha)

---

## 4. Proposta de Solução

### 4.1 Solução Principal (Recomendada)

**Objetivo:** Permitir editar usuário sem alterar a senha, e proteger o campo de senha adequadamente.

#### **A. Modificar `UsuarioRequestDTO`** — Tornar senha opcional

```java
public record UsuarioRequestDTO(
    @NotBlank(message = "Nome é obrigatório")
    String nome,

    String documento,

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    String email,

    @Size(min = 3, message = "Senha deve ter no mínimo 3 caracteres")
    String senha,  // ← REMOVIDO @NotBlank — agora OPCIONAL

    @NotNull(message = "Nível de acesso é obrigatório")
    NivelAcesso nivel,

    Long idPessoa,

    String foto
) {}
```

#### **B. Modificar `UsuarioService.salvar()`** — Não sobrescrever senha se vazia

```java
if (isNew) {
    // Novo usuário: sempre define senha temporária
    usuario.setSenha(passwordEncoder.encode(SENHA_TEMPORARIA));
    usuario.setSenhaTemporaria(true);
} else if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
    // Atualização: SOMENTE codifica se uma nova senha foi informada
    usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
    // Se era temporária, marca como não-temporária
    if (usuario.isSenhaTemporaria()) {
        usuario.setSenhaTemporaria(false);
    }
}
// Se a senha é null/vazia na atualização, mantém a senha anterior (sem fazer nada)
```

#### **C. Frontend — Comportamento Já Resolvido**

Com a correção do Bug 1:
- ✅ Endpoint `GET /api/usuarios/{id}` não retorna `senha`
- ✅ Campo de senha não é preenchido com hash
- ✅ Usuário vê campo vazio por padrão

#### **D. Frontend — Adicionar Botão Mostrar/Ocultar Senha (Requisito 4.4)**

O `GenericFormComponent` já renderiza campos de `type: password`, mas é recomendado adicionar um ícone de olho para:
- Alternar entre `type="password"` e `type="text"`
- Melhorar UX ao digitar senhas

**Modificação em `generic-form.component.html`:**
```html
<!-- Input padrão com toggle para password -->
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
```

**Modificação em `generic-form.component.ts`:**
```typescript
showPassword: Record<string, boolean> = {};

togglePasswordVisibility(fieldName: string): void {
  this.showPassword[fieldName] = !this.showPassword[fieldName];
}
```

---

## 5. Arquivos Que Serão Modificados

| Arquivo | Mudança | Justificativa |
|---------|---------|---------------|
| `UsuarioRequestDTO.java` | Remover `@NotBlank` de `senha` | Permitir senha opcional |
| `UsuarioService.java` | Modificar lógica de `salvar()` | Não sobrescrever senha se vazia |
| `generic-form.component.html` | Adicionar toggle password | Melhor UX para campos de senha |
| `generic-form.component.ts` | Adicionar método `togglePasswordVisibility()` | Controlar visibilidade |

---

## 6. Impactos e Riscos

### Impactos Positivos ✅
- Campo de senha não exibe hash na edição
- Usuário pode editar sem alterar senha
- Segue o label do frontend: "deixe vazio para manter"
- Melhora UX com botão mostrar/ocultar senha

### Riscos Mitigados ⚠️

| Risco | Mitigação |
|-------|-----------|
| Alguém deixa senha vazia sem querer | Mensagens claras: "deixe vazio para manter", validação no frontend |
| Senha `null` é enviada ao backend | Usar `.isEmpty()` para verificar tanto `null` quanto string vazia |
| Quebra compatibilidade com criação de usuário | Criação é **isNew**, usa `SENHA_TEMPORARIA` — não é afetada |
| Campos obrigatórios deixam de validar | Apenas `senha` fica opcional; `nome`, `email`, `documento`, `nivel` continuam obrigatórios |

---

## 7. Plano de Testes

### 7.1 Teste de Edição Sem Alterar Senha

**Cenário:** Editar usuário deixando campo de senha vazio

**Passos:**
1. Navegar para `/usuarios/editar/1`
2. Modificar nome: "João Silva" → "João Silva Pereira"
3. Deixar campo de senha **vazio**
4. Clicar "Atualizar"

**Esperado:**
- ✅ Requisição PUT enviada sem campo `senha` ou com `senha: null`
- ✅ Backend não sobrescreve a senha existente
- ✅ Usuário consegue fazer login com a senha anterior
- ✅ Mensagem de sucesso: "Registro atualizado com sucesso!"

### 7.2 Teste de Edição Alterando Senha

**Cenário:** Editar usuário E alterar a senha

**Passos:**
1. Navegar para `/usuarios/editar/1`
2. Modificar nome e digitar nova senha: "novaSenha123"
3. Clicar "Atualizar"

**Esperado:**
- ✅ Requisição PUT enviada com `senha: "novaSenha123"`
- ✅ Backend codifica a nova senha
- ✅ Usuário consegue fazer login com a nova senha
- ✅ Senha anterior não funciona mais

### 7.3 Teste: Campo Senha Não Exibe Hash

**Cenário:** Abrir formulário de edição

**Passos:**
1. Navegar para `/usuarios/editar/1`
2. Observar o campo de senha

**Esperado:**
- ✅ Campo de senha **vazio** (não preenche com hash)
- ✅ Sem caracteres visíveis (ou asteriscos)

### 7.4 Teste: Botão Mostrar/Ocultar Senha

**Cenário:** Editar usuário e digitar nova senha

**Passos:**
1. Navegar para `/usuarios/editar/1`
2. Digitar senha no campo
3. Clicar ícone de olho

**Esperado:**
- ✅ Ícone muda de olho aberto para olho fechado
- ✅ Senha fica visível (type muda para "text")
- ✅ Clicar novamente oculta a senha

### 7.5 Teste: Validação de Senha Obrigatória em Criação

**Cenário:** Criar novo usuário

**Passos:**
1. Navegar para `/usuarios/novo`
2. Preencher nome, email, documento, nível
3. Deixar campo de senha vazio
4. Clicar "Salvar"

**Esperado:**
- ❌ Erro de validação: "Senha é obrigatória"
- ✅ Usuário **não consegue** salvar sem senha em criação

### 7.6 Teste: Criar Novo Usuário com Senha

**Cenário:** Criar novo usuário normalmente

**Passos:**
1. Navegar para `/usuarios/novo`
2. Preencher todos os campos (incluindo senha)
3. Clicar "Salvar"

**Esperado:**
- ✅ Usuário criado com sucesso
- ✅ Senha é armazenada com hash no banco
- ✅ Usuário consegue fazer login com a senha fornecida

### 7.7 Teste: Compatibilidade com Resetar Senha

**Cenário:** Resetar senha via botão "Resetar Senha"

**Passos:**
1. Na listagem `/usuarios`, clicar botão "Resetar Senha" do usuário
2. Confirmar ação

**Esperado:**
- ✅ Senha alterada para "12345678"
- ✅ Flag `senhaTemporaria` marcada como `true`
- ✅ Usuário vê banner na próxima edição: "Senha Temporária"

---

## 8. Dependências e Compatibilidade

✅ **Sem dependências externas adicionais**
- Usa apenas Spring Security (já presente)
- Usa apenas validações do Jakarta Validation (já presentes)

✅ **Compatível com:**
- Bug 1 (usar DTOs) — complementa a correção
- Endpoints de autenticação (`/auth/alterar-senha`, etc.) — não são afetados
- Endpoints de Meu Ministério — permissões diferentes, não são afetados

---

## 9. Resumo Executivo

### Problema
Campo de senha exibe hash ao editar usuário; backend rejeita PUT sem nova senha.

### Causa Raiz
1. Endpoint retornava senha (agora resolvido pelo Bug 1)
2. `UsuarioRequestDTO` obriga senha em updates (será resolvido)
3. Backend sobrescreve senha mesmo quando não há intenção de alterá-la (será resolvido)

### Solução
1. Tornar campo `senha` opcional em `UsuarioRequestDTO`
2. Modificar lógica de `salvar()` para não sobrescrever senha se vazia
3. Adicionar toggle mostrar/ocultar senha no frontend

### Segurança
- ✅ Senha nunca é exposta em resposta de API
- ✅ Validações mantidas (criação ainda exige senha)
- ✅ Codificação de senha continua usando encoder seguro

---

## Próximo Passo

✅ **Análise concluída. Aguardando autorização para implementar a correção do Bug 2.**
