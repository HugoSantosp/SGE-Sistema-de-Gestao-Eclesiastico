# Bug 1 - Investigação: Líder do Ministério não consegue Editar/Excluir Escalas

## Resumo Executivo

O líder do ministério **não consegue editar o nome da escala** após criá-la e **não consegue deletar escalas** porque:

1. **Falta endpoint `PUT/PATCH` para editar o título da escala** — o backend não expõe nenhuma rota para atualização do nome.
2. **Falta endpoint `DELETE` com validação de autorização** — apesar de existir um `DELETE` no `EscalaController`, ele não valida se o usuário é o líder.
3. **Frontend não tem método para deletar escala** — o serviço `MmApiService` não possui método `deletarEscala()`.
4. **Inconsistência entre camadas**: 
   - O `MeuMinisterioController` (app do usuário) possui endpoints validados com `validarLiderDaEscala()`
   - Mas o `EscalaController` (endpoint genérico) não valida autorização

---

## Análise Detalhada

### Problema 1: Edição do Nome da Escala

#### Frontned (o que o usuário vê)

**Arquivo**: `sg-frontend/projects/meu-ministerio/src/app/features/escala-gerenciar/escala-gerenciar.component.html`

- O componente exibe o título da escala: `{{ escala?.titulo || 'Carregando...' }}`
- **Não há nenhum campo de input para editar o título**
- **Não há botão "Editar Nome"** nem modal para isso

#### Backend (o que existe)

**EscalaController.java** (`/api/escalas`):
```java
@GetMapping("/{id}")
@PostMapping
@PatchMapping("/{id}/toggle")
@DeleteMapping("/{id}")
// ❌ FALTA: PUT/PATCH para editar o título
```

**EscalaRequestDTO.java**:
```java
public record EscalaRequestDTO(
    String titulo,
    Long ministerioId
) {}
```
- DTO existe, mas não é usado para edição — apenas para criação

**MeuMinisterioService.java**:
```java
@Transactional
public EscalaResponseDTO criarEscala(Long ministerioId, String titulo, Usuario usuario) {
    validarLider(usuario, ministerioId);
    return escalaService.criar(new EscalaRequestDTO(titulo, ministerioId));
}
// ❌ FALTA: método atualizarEscala() ou similar
```

**MeuMinisterioController.java**:
```java
@PostMapping("/ministerios/{ministerioId}/escalas")
public ResponseEntity<EscalaResponseDTO> criarEscala(...) { ... }
// ❌ FALTA: PUT/PATCH para editar escala existente
```

#### Conclusão
**Não existe nenhuma rota para editar o título da escala.** O backend não oferece um endpoint PUT/PATCH e o frontend não oferece um campo para editar.

---

### Problema 2: Exclusão da Escala

#### Frontend

**MmApiService** (`mm-api.service.ts`):
```typescript
// Listagem e criação existem:
minhasEscalas(): Observable<EscalaResponse[]> { ... }
criarEscala(ministerioId: number, titulo: string): Observable<EscalaResponse> { ... }

// ❌ FALTA: deletarEscala(escalaId: number): Observable<void> { ... }
```

**EscalaGerenciarComponent**:
```typescript
// Não há nenhum método deletar()
// Não há nenhum botão "Deletar Escala" no template
```

#### Backend

**EscalaController** (`/api/escalas`):
```java
@DeleteMapping("/{id}")
@Operation(summary = "Deletar escala")
public ResponseEntity<Void> deletar(@PathVariable Long id) {
    escalaService.deletar(id);
    return ResponseEntity.noContent().build();
}
```
- ❌ **NÃO VALIDA AUTORIZAÇÃO** — qualquer usuário autenticado pode deletar qualquer escala

**EscalaService.deletar()**:
```java
@Transactional
public void deletar(Long id) {
    if (!escalaRepository.existsById(id)) {
        throw new ResourceNotFoundException("Escala", id);
    }
    escalaRepository.deleteById(id);
}
```
- ❌ **NENHUMA VALIDAÇÃO DE PERMISSÕES** — apenas verifica existência

**MeuMinisterioController** (`/api/meu-ministerio/escalas`):
- ❌ **NÃO EXISTE** endpoint DELETE com validação `validarLiderDaEscala()`

**MeuMinisterioService**:
- ❌ **NÃO EXISTE** método `deletarEscala()` que valida autorização

#### Conclusão
O endpoint DELETE existe no `EscalaController`, mas:
1. Não valida se o usuário é o líder da escala
2. Está em `/api/escalas/{id}` (rota genérica) em vez de `/api/meu-ministerio/escalas/{id}`
3. O frontend não oferece botão nem chamada para deletar

---

### Problema 3: Arquitetura Inconsistente

**Padrão observado**:

O projeto adota uma **arquitetura em duas camadas de endpoints**:

1. **`/api/escalas`** (EscalaController) — operações genéricas de CRUD
   - Sem validação de autorização
   - Sem considerar o contexto de "meu ministério"

2. **`/api/meu-ministerio/escalas`** (MeuMinisterioController) — operações no contexto do app MeuMinisterio
   - Valida autorização com `validarLiderDaEscala()`
   - Garante que o usuário é membro do ministério

**O problema**: O frontend usa endpoints de `/api/meu-ministerio`, que está correto. Mas:
- Não há rota PUT/PATCH para editar no MeuMinisterio
- Não há rota DELETE validada no MeuMinisterio

---

## Plano de Correção

### Fase 1: Backend — Adicionar Endpoint para Editar Título

**Arquivo**: `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioService.java`

```java
@Transactional
public EscalaResponseDTO atualizarEscala(Long escalaId, String novoTitulo, Usuario usuario) {
    validarLiderDaEscala(usuario, escalaId);
    Escala escala = escalaRepository.findById(escalaId)
        .orElseThrow(() -> new ResourceNotFoundException("Escala", escalaId));
    escala.setTitulo(novoTitulo);
    escalaRepository.save(escala);
    return EscalaResponseDTO.fromEntity(escala, escala.getDatas().size(), escala.getConfirmacoes().size());
}
```

**Arquivo**: `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioController.java`

```java
@PutMapping("/escalas/{escalaId}")
@Operation(summary = "Editar título da escala (líder/pastor)")
public ResponseEntity<EscalaResponseDTO> atualizarEscala(
        @PathVariable Long escalaId,
        @RequestBody EscalaUpdateRequestDTO dto,
        Authentication authentication) {
    return ResponseEntity.ok(meuMinisterioService.atualizarEscala(
            escalaId, dto.titulo(), usuarioLogado(authentication)));
}
```

**Novo DTO**: `sg-backend/src/main/java/com/sg/meuministerio/dto/EscalaUpdateRequestDTO.java`

```java
public record EscalaUpdateRequestDTO(
    @NotBlank(message = "Título é obrigatório")
    String titulo
) {}
```

---

### Fase 2: Backend — Adicionar Endpoint para Deletar Escala

**Arquivo**: `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioService.java`

```java
@Transactional
public void deletarEscala(Long escalaId, Usuario usuario) {
    validarLiderDaEscala(usuario, escalaId);
    escalaRepository.deleteById(escalaId);
}
```

**Arquivo**: `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioController.java`

```java
@DeleteMapping("/escalas/{escalaId}")
@Operation(summary = "Deletar escala (líder/pastor)")
public ResponseEntity<Void> deletarEscala(
        @PathVariable Long escalaId,
        Authentication authentication) {
    meuMinisterioService.deletarEscala(escalaId, usuarioLogado(authentication));
    return ResponseEntity.noContent().build();
}
```

---

### Fase 3: Frontend — Adicionar Métodos ao Serviço de API

**Arquivo**: `sg-frontend/projects/meu-ministerio/src/app/core/services/mm-api.service.ts`

```typescript
atualizarEscala(escalaId: number, titulo: string): Observable<EscalaResponse> {
  return this.http.put<EscalaResponse>(`${this.baseUrl}/escalas/${escalaId}`, { titulo });
}

deletarEscala(escalaId: number): Observable<void> {
  return this.http.delete<void>(`${this.baseUrl}/escalas/${escalaId}`);
}
```

---

### Fase 4: Frontend — Adicionar UI para Editar e Deletar

**Arquivo**: `sg-frontend/projects/meu-ministerio/src/app/features/escala-gerenciar/escala-gerenciar.component.ts`

```typescript
// Adicionar variáveis de estado
editandoTitulo = false;
novoTitulo = '';
editandoTituloLoading = false;

// Método para abrir edição
iniciarEdicaoTitulo(): void {
  this.novoTitulo = this.escala?.titulo || '';
  this.editandoTitulo = true;
}

// Método para salvar título
salvarTitulo(): void {
  if (!this.escalaId || !this.novoTitulo.trim()) {
    this.error = 'Informe um título válido';
    return;
  }
  this.editandoTituloLoading = true;
  this.error = '';
  this.api.atualizarEscala(this.escalaId, this.novoTitulo).subscribe({
    next: (escalaNova) => {
      this.editandoTituloLoading = false;
      this.escala = { ...this.escala, titulo: escalaNova.titulo };
      this.editandoTitulo = false;
      this.aviso = 'Título atualizado com sucesso!';
    },
    error: () => {
      this.editandoTituloLoading = false;
      this.error = 'Erro ao atualizar o título.';
    }
  });
}

// Método para deletar escala
deletarEscala(): void {
  if (!this.escalaId) return;
  if (!confirm('Tem certeza que deseja deletar esta escala? Esta ação não pode ser desfeita.')) {
    return;
  }
  this.api.deletarEscala(this.escalaId).subscribe({
    next: () => {
      this.router.navigate(['/escalas']);
    },
    error: () => {
      this.error = 'Erro ao deletar a escala.';
    }
  });
}

cancelarEdicaoTitulo(): void {
  this.editandoTitulo = false;
  this.novoTitulo = '';
}
```

**Arquivo**: `sg-frontend/projects/meu-ministerio/src/app/features/escala-gerenciar/escala-gerenciar.component.html`

Adicionar ao header (seção "Montar Escala"):
```html
<!-- Seção de edição do título -->
<div class="mm-card">
  <div *ngIf="!editandoTitulo" class="d-flex justify-content-between align-items-center">
    <div>
      <div class="mm-card-title">{{ escala?.titulo }}</div>
      <div class="mm-card-sub">Criada em {{ escala?.createdAt | date:'d/M/yyyy' }}</div>
    </div>
    <div class="d-flex gap-2">
      <button class="btn btn-sm btn-outline-primary" (click)="iniciarEdicaoTitulo()">
        <i class="bi bi-pencil-square me-1"></i> Editar título
      </button>
      <button class="btn btn-sm btn-outline-danger" (click)="deletarEscala()">
        <i class="bi bi-trash me-1"></i> Deletar escala
      </button>
    </div>
  </div>
  <div *ngIf="editandoTitulo" class="row g-2">
    <div class="col-12">
      <label class="form-label">Novo título *</label>
      <input class="form-control" type="text" [(ngModel)]="novoTitulo" 
             [disabled]="editandoTituloLoading" placeholder="Digite o novo título">
    </div>
    <div class="col-12 d-flex gap-2">
      <button class="btn btn-sm btn-primary" (click)="salvarTitulo()" 
              [disabled]="editandoTituloLoading">
        <i class="bi bi-check-circle me-1"></i> Salvar
      </button>
      <button class="btn btn-sm btn-outline-secondary" (click)="cancelarEdicaoTitulo()" 
              [disabled]="editandoTituloLoading">
        <i class="bi bi-x-circle me-1"></i> Cancelar
      </button>
    </div>
  </div>
</div>
```

---

## Validação de Segurança

✅ **Autorização no Backend**: `validarLiderDaEscala()` valida que o usuário é líder do ministério  
✅ **Sem Exposição de Dados**: DTOs retornam apenas dados públicos  
✅ **Transações ACID**: `@Transactional` garante consistência  
✅ **Sem Bypass de Autorização**: Frontend não chama `EscalaController` direto, usa `MeuMinisterioController`

---

## Plano de Testes

### Teste 1: Edição do Título

1. **Setup**: Líder logado, com uma escala criada
2. **Ação**: Clica em "Editar título", muda o nome, clica "Salvar"
3. **Esperado**: 
   - Título é atualizado na tela
   - Mensagem de sucesso aparece
   - Ao recarregar, o novo título persiste

### Teste 2: Tentativa de Edição sem Permissão

1. **Setup**: Membro padrão (não líder) logado
2. **Ação**: Envia `PUT /api/meu-ministerio/escalas/{id}` manualmente via `curl` ou Postman
3. **Esperado**: Status 403 com mensagem "Acesso negado: apenas o líder deste ministério pode realizar esta ação"

### Teste 3: Exclusão de Escala

1. **Setup**: Líder logado, com uma escala criada
2. **Ação**: Clica em "Deletar escala", confirma no modal, clica "OK"
3. **Esperado**:
   - Escala é deletada
   - Redireciona para `/escalas`
   - Escala não aparece mais na lista

### Teste 4: Tentativa de Exclusão sem Permissão

1. **Setup**: Membro padrão logado
2. **Ação**: Envia `DELETE /api/meu-ministerio/escalas/{id}` manualmente
3. **Esperado**: Status 403 com mensagem de acesso negado

### Teste 5: Persistência em Banco de Dados

1. **Setup**: Líder edita o título de uma escala
2. **Ação**: Consulta diretamente o banco de dados (`SELECT * FROM escala WHERE id = ?`)
3. **Esperado**: Campo `titulo` contém o novo valor

---

## Arquivos a Serem Modificados

**Backend** (3 arquivos):
- `MeuMinisterioService.java` — adicionar métodos `atualizarEscala()` e `deletarEscala()`
- `MeuMinisterioController.java` — adicionar endpoints `PUT` e `DELETE`
- `EscalaUpdateRequestDTO.java` — novo DTO

**Frontend** (2 arquivos):
- `mm-api.service.ts` — adicionar métodos `atualizarEscala()` e `deletarEscala()`
- `escala-gerenciar.component.ts` — adicionar lógica e UI para edição e exclusão
- `escala-gerenciar.component.html` — adicionar formulário e botões

---

## Impactos Esperados

✅ **Positivos**:
- Líder consegue editar o nome da escala após criação
- Líder consegue deletar escalas que não quer mais
- Usuários padrão continuam sem acesso (autorização validada)
- Consistência entre backend e frontend

⚠️ **Verificar**:
- Se há cascatas de exclusão definidas (datas, designações, músicas, confirmações devem ser deletadas junto)
- Se há validações de negócio que impedem deletar escalas com confirmações

---

## Conclusão

O problema é **FALTA DE FUNCIONALIDADE**, não um bug de código defeituoso. Ambas as operações (editar e deletar) precisam ser implementadas do zero, seguindo o padrão de autorização já presente no `MeuMinisterioService`.

A correção é **SEGURA** porque usa a validação existente `validarLiderDaEscala()`, que verifica se o usuário é líder do ministério ao qual a escala pertence.

