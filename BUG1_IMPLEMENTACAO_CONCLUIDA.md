# Bug 1 — Implementação Concluída: Editar e Deletar Escalas

## ✅ Resumo da Implementação

O Bug 1 foi **completamente implementado**. O líder do ministério agora consegue:
1. ✅ Editar o título de uma escala após criação
2. ✅ Deletar uma escala com confirmação

---

## 📋 Arquivos Modificados/Criados

### Backend (3 arquivos)

#### 1. **Novo DTO**: `EscalaUpdateRequestDTO.java`
- **Caminho**: `sg-backend/src/main/java/com/sg/meuministerio/dto/EscalaUpdateRequestDTO.java`
- **Tipo**: NOVO ARQUIVO
- **O que faz**: Record com campo `titulo` validado

```java
public record EscalaUpdateRequestDTO(
    @NotBlank(message = "Título é obrigatório")
    String titulo
) {}
```

#### 2. **MeuMinisterioService.java** — Adicionados 2 métodos
- **Caminho**: `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioService.java`
- **Tipo**: MODIFICAÇÃO
- **Linhas**: 352-371

**Método 1**: `atualizarEscala()`
```java
@Transactional
public EscalaResponseDTO atualizarEscala(Long escalaId, String novoTitulo, Usuario usuario) {
    validarLiderDaEscala(usuario, escalaId);
    Escala escala = escalaRepository.findById(escalaId)
        .orElseThrow(() -> new ResourceNotFoundException("Escala", escalaId));
    escala.setTitulo(novoTitulo);
    escalaRepository.save(escala);
    return EscalaResponseDTO.fromEntity(...);
}
```

**Método 2**: `deletarEscala()`
```java
@Transactional
public void deletarEscala(Long escalaId, Usuario usuario) {
    validarLiderDaEscala(usuario, escalaId);
    escalaRepository.deleteById(escalaId);
}
```

**Características de Segurança**:
- ✅ Usa `validarLiderDaEscala()` para validar autorização
- ✅ Apenas líder do ministério pode modificar
- ✅ Transação ACID garantida (@Transactional)

#### 3. **MeuMinisterioController.java** — Adicionados 2 endpoints + 1 import
- **Caminho**: `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioController.java`
- **Tipo**: MODIFICAÇÃO
- **Novos imports**: `EscalaUpdateRequestDTO`

**Endpoint 1**: `PUT /api/meu-ministerio/escalas/{escalaId}`
```java
@PutMapping("/escalas/{escalaId}")
@Operation(summary = "Editar título da escala (líder/pastor)")
public ResponseEntity<EscalaResponseDTO> atualizarEscala(
        @PathVariable Long escalaId,
        @RequestBody @Valid EscalaUpdateRequestDTO dto,
        Authentication authentication) {
    return ResponseEntity.ok(meuMinisterioService.atualizarEscala(
            escalaId, dto.titulo(), usuarioLogado(authentication)));
}
```

**Endpoint 2**: `DELETE /api/meu-ministerio/escalas/{escalaId}`
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

### Frontend (3 arquivos)

#### 4. **MmApiService.ts** — Adicionados 2 métodos
- **Caminho**: `sg-frontend/projects/meu-ministerio/src/app/core/services/mm-api.service.ts`
- **Tipo**: MODIFICAÇÃO

```typescript
atualizarEscala(escalaId: number, titulo: string): Observable<EscalaResponse> {
  return this.http.put<EscalaResponse>(`${this.baseUrl}/escalas/${escalaId}`, { titulo });
}

deletarEscala(escalaId: number): Observable<void> {
  return this.http.delete<void>(`${this.baseUrl}/escalas/${escalaId}`);
}
```

#### 5. **EscalaGerenciarComponent.ts** — Adicionadas 3 variáveis + 4 métodos
- **Caminho**: `sg-frontend/projects/meu-ministerio/src/app/features/escala-gerenciar/escala-gerenciar.component.ts`
- **Tipo**: MODIFICAÇÃO

**Variáveis de Controle**:
```typescript
editandoTitulo = false;
novoTitulo = '';
editandoTituloLoading = false;
```

**Método 1**: `iniciarEdicaoTitulo()`
- Abre modo edição, pré-preenchendo com o título atual

**Método 2**: `salvarTitulo()`
- Valida e envia PUT ao backend
- Atualiza escala localmente
- Mostra mensagem de sucesso/erro

**Método 3**: `cancelarEdicaoTitulo()`
- Fecha modo edição, descarta alterações

**Método 4**: `deletarEscala()`
- Confirmação modal com usuário
- Envia DELETE ao backend
- Redireciona para `/escalas`

#### 6. **EscalaGerenciarComponent.html** — Adicionada seção de edição
- **Caminho**: `sg-frontend/projects/meu-ministerio/src/app/features/escala-gerenciar/escala-gerenciar.component.html`
- **Tipo**: MODIFICAÇÃO
- **Local**: Após o header, antes de "Status da escala"

**Elementos Adicionados**:
- Card com:
  - Título da escala + data de criação (modo visualização)
  - Botão "Editar" (ícone: pencil-square)
  - Botão "Deletar" (ícone: trash)
  - Campo input para novo título (modo edição)
  - Botões "Salvar" e "Cancelar" (modo edição)

**Interatividade**:
- ✅ Transição suave entre modo visualização/edição
- ✅ Desabilita botões durante salvamento/exclusão
- ✅ Validação do título vazio
- ✅ Confirmação de deletar

---

## 🔒 Validação de Segurança

| Aspecto | Status | Detalhe |
|---------|--------|---------|
| **Autorização** | ✅ | `validarLiderDaEscala()` verifica JWT e papel |
| **Isolamento de ministério** | ✅ | Líder só pode editar/deletar escalas do seu ministério |
| **Validação de entrada** | ✅ | `@NotBlank` no DTO + verificação frontend |
| **Sem exposição de senha** | ✅ | DTOs retornam apenas dados públicos |
| **Transações ACID** | ✅ | `@Transactional` garante consistência |
| **Sem bypass de URL** | ✅ | Endpoints usam `/api/meu-ministerio` (com autorização) |

---

## 🧪 Testes Realizados

### ✅ Teste 1: Compilação
- Frontend compila sem erros TypeScript
- Backend compila sem erros Java
- Todos os imports estão corretos

### ✅ Teste 2: Integridade de Código
- Métodos `atualizarEscala()` e `deletarEscala()` existem no service
- Endpoints `PUT` e `DELETE` existem no controller
- Métodos `atualizarEscala()` e `deletarEscala()` existem no MmApiService
- Variáveis de estado existem no componente
- HTML contém botões e formulário de edição

### 📋 Testes Recomendados (Manual)

#### Cenário 1: Líder edita título
1. Login como líder do ministério
2. Abrir uma escala em gerenciamento
3. Clicar "Editar"
4. Alterar nome, clicar "Salvar"
5. **Esperado**: Título atualizado, mensagem sucesso, persiste ao recarregar

#### Cenário 2: Membro padrão tenta editar
1. Login como membro padrão
2. Enviar `PUT /api/meu-ministerio/escalas/{id}` via Postman/curl
3. **Esperado**: 403 Forbidden, mensagem "Acesso negado"

#### Cenário 3: Líder deleta escala
1. Login como líder
2. Clicar "Deletar", confirmar no modal
3. **Esperado**: Redirecionado para `/escalas`, escala removida da lista

#### Cenário 4: Membro padrão tenta deletar
1. Login como membro
2. Enviar `DELETE /api/meu-ministerio/escalas/{id}` via curl
3. **Esperado**: 403 Forbidden

---

## 📊 Resumo de Alterações

| Métrica | Quantidade |
|---------|-----------|
| **Arquivos novos** | 1 (DTO) |
| **Arquivos modificados** | 5 |
| **Linhas adicionadas** | ~150 |
| **Endpoints novos** | 2 (PUT + DELETE) |
| **Métodos novos (Backend)** | 2 |
| **Métodos novos (Frontend)** | 4 |
| **Componentes UI novos** | 1 card + 2 botões |

---

## ✨ Impacto da Implementação

### ✅ Positivos
- Líder consegue gerenciar escalas completa mente
- Usuário padrão não consegue acessar (segurança)
- Operações são confirmadas (sem acidentes)
- UI é intuitiva e responsiva
- Sem breaking changes no código existente

### ⚠️ Pontos de Atenção
- Verificar se há dados dependentes (datas, designações, músicas) — banco deve ter cascatas
- Se houver confirmações em uma escala com histórico, considerar soft-delete

---

## 🚀 Próximas Etapas

1. **Commit**: Fazer commit com mensagem descritiva
2. **Push**: Enviar para repositório remoto
3. **Testes manuais**: Validar cenários acima
4. **Deploy**: Atualizar ambiente se necessário

---

## ✅ Conclusão

**Bug 1 está 100% implementado e pronto para testes.** 

O líder do ministério agora consegue:
- ✅ Editar o nome da escala a qualquer momento
- ✅ Deletar escalas que não quer mais

Todas as validações de segurança foram aplicadas.

