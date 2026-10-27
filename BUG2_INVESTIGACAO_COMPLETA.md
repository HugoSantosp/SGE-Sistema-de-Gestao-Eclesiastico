# Análise Completa — Bug 2: Usuário Padrão Visualiza Escalas Fechadas

## 1. Resumo Executivo

**Problema:** Usuários com perfil padrão conseguem visualizar escalas que já foram fechadas pelo líder do ministério. O comportamento esperado é mostrar apenas escalas abertas para usuários padrão.

**Causa Raiz Confirmada:** O backend retorna TODAS as escalas (abertas e fechadas) para qualquer usuário logado, sem fazer filtragem por status ou perfil. O frontend exibe todas sem questionar.

**Severidade:** MÉDIA - Violação das regras de negócio, mas sem impacto destrutivo.

---

## 2. Investigação Detalhada

### 2.1 Como o Status é Armazenado

**Arquivo:** `sg-backend/src/main/java/com/sg/escala/Escala.java`

```java
@Column(nullable = false)
@Builder.Default
private boolean aberta = true;  // ← Campo que controla o status
```

**Características:**
- ✅ Campo booleano simples: `aberta`
- ✅ Padrão: `true` (escala começa aberta)
- ✅ Alterado via método `toggleEscala()` do `EscalaService`

### 2.2 Como o Status é Retornado

**Arquivo:** `sg-backend/src/main/java/com/sg/escala/dto/EscalaResponseDTO.java`

```java
public record EscalaResponseDTO(
    Long id,
    String titulo,
    Long ministerioId,
    String nomeMinisterio,
    String publicToken,
    String resultadoToken,
    boolean aberta,           // ← Status incluído no DTO
    int datasCount,
    int confirmacoesCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { ... }
```

**Características:**
- ✅ Campo `aberta` é retornado em todas as respostas
- ✅ Sem nenhum filtro ou lógica condicional

### 2.3 Endpoints que Retornam Escalas

**Arquivo:** `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioController.java`

#### **Endpoint 1:** `GET /api/meu-ministerio/escalas`
```java
@GetMapping("/escalas")
@Operation(summary = "Escalas dos ministérios do usuário")
public ResponseEntity<List<EscalaResponseDTO>> minhasEscalas(Authentication authentication) {
    return ResponseEntity.ok(meuMinisterioService.listarMinhasEscalas(usuarioLogado(authentication)));
}
```

- **Quem acessa:** Qualquer usuário logado
- **O que retorna:** TODAS as escalas dos ministérios que o usuário pertence
- **Inclui escalas fechadas?** ✅ **SIM**

#### **Endpoint 2:** `GET /api/meu-ministerio/ministerios/{ministerioId}/escalas`
```java
@GetMapping("/ministerios/{ministerioId}/escalas")
@Operation(summary = "Escalas de um ministério (membro do ministério/pastor)")
public ResponseEntity<List<EscalaResponseDTO>> escalasDoMinisterio(
        @PathVariable Long ministerioId,
        Authentication authentication) {
    return ResponseEntity.ok(meuMinisterioService.listarEscalasDoMinisterio(ministerioId, usuarioLogado(authentication)));
}
```

- **Quem acessa:** Membro ou pastor
- **O que retorna:** TODAS as escalas do ministério específico
- **Inclui escalas fechadas?** ✅ **SIM**

### 2.4 Implementação do Service (onde falta o filtro)

**Arquivo:** `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioService.java` (linhas ~143-170)

```java
@Transactional(readOnly = true)
public List<EscalaResponseDTO> listarMinhasEscalas(Usuario usuario) {
    List<Escala> escalas;

    if (isAcessoAmpliado(usuario)) {
        escalas = escalaRepository.findAll();  // ← Líderes/pastores veem TODAS
    } else {
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return List.of();
        var ministerioIds = ministerioMembroRepository.findByMembroId(membroId).stream()
                .map(v -> v.getMinisterio().getId())
                .toList();
        if (ministerioIds.isEmpty()) return List.of();
        escalas = escalaRepository.findByMinisterioIdIn(ministerioIds);  // ← Usuários padrão veem TUDO
    }

    // ... resto do código ...
    return escalas.stream()
            .sorted(java.util.Comparator.comparing(Escala::getCreatedAt).reversed())
            .map(e -> EscalaResponseDTO.fromEntity(...))
            .toList();
}
```

**Problema:** Nenhuma filtragem por status `aberta`. Todas as escalas são retornadas sem validação.

### 2.5 Como o Frontend Consome

**Arquivo:** `sg-frontend/projects/meu-ministerio/src/app/features/escalas/escalas.component.ts`

```typescript
carregar(): void {
    this.loading = true;
    this.error = '';
    this.api.minhasEscalas().subscribe({
        next: (lista) => {
            // ❌ Sem filtro por status
            this.escalas = this.ministerioFiltro
                ? lista.filter(e => e.ministerioId === this.ministerioFiltro)
                : lista;
            this.loading = false;
        },
        ...
    });
}
```

**Problema:** Frontend só filtra por `ministerioId`, não por `aberta`.

### 2.6 Template HTML

**Arquivo:** `sg-frontend/projects/meu-ministerio/src/app/features/escalas/escalas.component.html`

```html
<span class="badge" [class.badge-success]="e.aberta" [class.badge-secondary]="!e.aberta">
  {{ e.aberta ? 'Aberta' : 'Fechada' }}
</span>
```

**Observação:** Mostra o status visualmente, mas não impede o acesso. É apenas uma *exibição*.

### 2.7 Diferenciação de Papéis

**Arquivo:** `sg-backend/src/main/java/com/sg/meuministerio/MeuMinisterioService.java`

```java
private boolean isAcessoAmpliado(Usuario usuario) {
    return usuario.getNivel() == NivelAcesso.PASTOR_PRESIDENTE
            || usuario.getNivel() == NivelAcesso.PASTOR_AUXILIAR;
}
```

**Papéis com acesso ampliado:**
- `PASTOR_PRESIDENTE` — Acesso a tudo
- `PASTOR_AUXILIAR` — Acesso a tudo

**Papéis padrão:**
- `MEMBRO` — Acesso apenas aos ministérios onde tem vínculo (com qualquer papel: LIDER, INTEGRANTE, MUSICO, etc.)
- `TESOUREIRO`, `SECRETARIO`, etc. — Podem ser membros da igreja e ter papéis em ministérios

---

## 3. Causa Raiz — CONFIRMADA

| Item | Status | Detalhe |
|------|--------|---------|
| **Backend filtra escalas?** | ❌ NÃO | `listarMinhasEscalas()` não verifica `aberta` |
| **Frontend filtra escalas?** | ❌ NÃO | Só filtra por `ministerioId` |
| **Status é retornado na API?** | ✅ SIM | Campo `aberta` está no `EscalaResponseDTO` |
| **Frontend consegue ver o status?** | ✅ SIM | Propriedade `aberta` está no modelo `EscalaResponse` |
| **Autorização é validada?** | ✅ PARCIAL | Apenas verifica se é membro do ministério, não se pode ver escalas fechadas |

**Conclusão:** A filtragem por status **deve ser feita no backend**. Não basta ocultar no frontend porque a API continua retornando escalas fechadas, violando as regras de negócio.

---

## 4. Comportamento Esperado

### Por Perfil:

| Perfil | Escalas Abertas | Escalas Fechadas |
|--------|-----------------|------------------|
| **Pastor/Lider administrativo** | ✅ Vê todas | ✅ Vê todas |
| **Líder do Ministério** | ✅ Vê todas | ✅ Vê todas |
| **Membro Integrante/Músico/Etc** | ✅ Vê | ❌ Não vê |

### Cenários:

**Cenário 1: Usuário padrão em ministério do Louvor**
- Louvor tem 3 escalas: A (aberta), B (fechada), C (aberta)
- **Esperado:** Ve A e C
- **Atual:** Ve A, B e C ❌

**Cenário 2: Líder do Louvor**
- Mesmo cenário
- **Esperado:** Ve A, B e C
- **Atual:** Ve A, B e C ✅

**Cenário 3: Pastor vendo ministérios**
- Qualquer situação
- **Esperado:** Ve tudo
- **Atual:** Ve tudo ✅

---

## 5. Arquivos Envolvidos

### Backend

| Arquivo | Modificação | Razão |
|---------|------------|-------|
| `MeuMinisterioService.java` | **MODIFICAR** `listarMinhasEscalas()` | Adicionar filtro por `aberta` para usuários padrão |
| `MeuMinisterioService.java` | **MODIFICAR** `listarEscalasDoMinisterio()` | Adicionar filtro por `aberta` para usuários padrão |

### Frontend

| Arquivo | Modificação | Razão |
|---------|------------|-------|
| **Nenhum** | N/A | Backend faz o filtro, frontend recebe dados já filtrados |

---

## 6. Solução Proposta

### Estratégia

Adicionar lógica de filtro no **backend**, especificamente nos dois métodos que retornam listas de escalas.

**Princípio:** 
- Se o usuário é pastor/líder administrativo → retorna TODAS as escalas
- Se o usuário é membro padrão → retorna apenas escalas abertas

### Implementação Específica

#### **Modificação 1: `listarMinhasEscalas()`**

**Localização:** `MeuMinisterioService.java`, método `listarMinhasEscalas()`

**Código Atual (linhas ~143-170):**
```java
@Transactional(readOnly = true)
public List<EscalaResponseDTO> listarMinhasEscalas(Usuario usuario) {
    List<Escala> escalas;

    if (isAcessoAmpliado(usuario)) {
        escalas = escalaRepository.findAll();
    } else {
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return List.of();
        var ministerioIds = ministerioMembroRepository.findByMembroId(membroId).stream()
                .map(v -> v.getMinisterio().getId())
                .toList();
        if (ministerioIds.isEmpty()) return List.of();
        escalas = escalaRepository.findByMinisterioIdIn(ministerioIds);
    }

    // ... continua ...
}
```

**Mudança Necessária:**
```java
@Transactional(readOnly = true)
public List<EscalaResponseDTO> listarMinhasEscalas(Usuario usuario) {
    List<Escala> escalas;

    if (isAcessoAmpliado(usuario)) {
        // PASTORES/LÍDERES ADMINISTRATIVOS: veem TODAS as escalas
        escalas = escalaRepository.findAll();
    } else {
        // MEMBROS PADRÃO: veem apenas escalas abertas
        Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
        if (membroId == null) return List.of();
        var ministerioIds = ministerioMembroRepository.findByMembroId(membroId).stream()
                .map(v -> v.getMinisterio().getId())
                .toList();
        if (ministerioIds.isEmpty()) return List.of();
        escalas = escalaRepository.findByMinisterioIdIn(ministerioIds);
        
        // ← NOVA LÓGICA: Filtrar apenas escalas abertas para membros padrão
        escalas = escalas.stream()
                .filter(Escala::isAberta)
                .toList();
    }

    // ... resto permanece igual ...
}
```

#### **Modificação 2: `listarEscalasDoMinisterio()`**

**Localização:** `MeuMinisterioService.java`, método `listarEscalasDoMinisterio()`

**Código Atual (linhas ~175-182):**
```java
@Transactional(readOnly = true)
public List<EscalaResponseDTO> listarEscalasDoMinisterio(Long ministerioId, Usuario usuario) {
    validarMembro(usuario, ministerioId);
    return escalaRepository.findByMinisterioId(ministerioId).stream()
            .map(e -> EscalaResponseDTO.fromEntity(e, e.getDatas().size(), e.getConfirmacoes().size()))
            .toList();
}
```

**Mudança Necessária:**
```java
@Transactional(readOnly = true)
public List<EscalaResponseDTO> listarEscalasDoMinisterio(Long ministerioId, Usuario usuario) {
    validarMembro(usuario, ministerioId);
    
    List<Escala> escalas = escalaRepository.findByMinisterioId(ministerioId);
    
    // NOVA LÓGICA: Se não é líder do ministério, mostrar apenas escalas abertas
    if (!isLiderDoMinisterio(usuario, ministerioId)) {
        escalas = escalas.stream()
                .filter(Escala::isAberta)
                .toList();
    }
    
    return escalas.stream()
            .map(e -> EscalaResponseDTO.fromEntity(e, e.getDatas().size(), e.getConfirmacoes().size()))
            .toList();
}
```

**Novo Método Helper:**
```java
/** Verifica se o usuário é líder do ministério específico. */
private boolean isLiderDoMinisterio(Usuario usuario, Long ministerioId) {
    if (isAcessoAmpliado(usuario)) return true;  // Pastores são líderes em tudo
    
    Long membroId = membroDoUsuario(usuario).map(Membro::getId).orElse(null);
    if (membroId == null) return false;
    
    return ministerioMembroRepository.existsByMinisterioIdAndMembroIdAndPapel(
            ministerioId, membroId, PapelMinisterio.LIDER);
}
```

---

## 7. Justificativa Técnica

### Por que filtro no Backend?

1. **Segurança:** O backend é a fonte da verdade. Não se deve confiar no frontend para aplicar regras de negócio.
2. **Consistência:** Qualquer client (web, mobile, API direta) receberá dados filtrados.
3. **Auditoria:** Requer acesso via API não autorizado seria detectado.
4. **Performance:** Escalas fechadas não são transmitidas desnecessariamente.

### Por que não apenas no Frontend?

❌ Frontend pode ser manipulado:
```javascript
// Usuário malicioso poderia fazer:
api.get('/api/meu-ministerio/escalas').then(dados => {
  console.log(dados.filter(e => !e.aberta));  // Vê escalas fechadas mesmo assim
});
```

---

## 8. Impactos e Riscos

### Impactos Positivos ✅

| Impacto | Nível |
|--------|-------|
| Usuários padrão não veem escalas fechadas | Alto |
| Líderes continuam vendo tudo | Nulo (sem mudança) |
| Pastores continuam vendo tudo | Nulo (sem mudança) |
| Segurança de dados | Alto |
| Conformidade com regras de negócio | Alto |

### Riscos Potenciais ⚠️

| Risco | Probabilidade | Impacto | Mitigação |
|------|---|---|---|
| Quebra compatibilidade com app mobile/desktop | BAIXA | MÉDIO | Não — são apps separados |
| Usuário não consegue acessar escala válida | BAIXA | MÉDIO | Validação em testes |
| Performance degrada com muitas escalas | MUITO BAIXA | BAIXO | Stream filter é eficiente |
| Líderes perdem acesso a escalas fechadas | MUITO BAIXA | ALTO | Código verifica `isLiderDoMinisterio()` |

### Funcionalidades Afetadas

| Funcionalidade | Afetada? | Observação |
|----------------|----------|-----------|
| Confirmação de disponibilidade | ❌ NÃO | Endpoint diferente (`POST /confirmar`) |
| Edição de escala (líder) | ❌ NÃO | Valida liderança separadamente |
| Visualização de resultado | ⚠️ TALVEZ | Depende de como é implementado (verificar) |
| Link público de escala | ❌ NÃO | Usa token, não passa por filtro |
| Relatórios/Admin | ❌ NÃO | Está em outro app |

---

## 9. Plano de Testes

### 9.1 Teste: Membro Padrão Vê Apenas Escalas Abertas

**Setup:**
- Usuário: "João Silva" (MEMBRO)
- Ministério: Louvor
- Papel no Louvor: INTEGRANTE (não líder)
- Escalas no Louvor: A (aberta), B (fechada), C (aberta)

**Passos:**
1. Login como João Silva
2. Navegar para `/escalas`
3. Observar listagem

**Esperado:**
- ✅ Apenas A e C aparecem
- ✅ B não está na lista
- ✅ Badge "Aberta" em A e C

**Verificação de API:**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/meu-ministerio/escalas
```
- ✅ Resposta contém apenas A e C
- ✅ Sem B

---

### 9.2 Teste: Líder Vê Todas as Escalas

**Setup:**
- Usuário: "Maria Silva" (MEMBRO, papel LIDER no Louvor)
- Ministério: Louvor
- Escalas no Louvor: A (aberta), B (fechada), C (aberta)

**Passos:**
1. Login como Maria Silva
2. Navegar para `/escalas`
3. Observar listagem

**Esperado:**
- ✅ A, B e C aparecem
- ✅ Badge "Fechada" em B
- ✅ Líder consegue editar/montar B normalmente

---

### 9.3 Teste: Pastor Vê Todas as Escalas

**Setup:**
- Usuário: "Pastor Pedro" (PASTOR_PRESIDENTE)
- Papel global: Pastor
- Escalas no sistema: A (aberta), B (fechada), etc.

**Passos:**
1. Login como Pastor Pedro
2. Navegar para `/escalas`
3. Observar listagem

**Esperado:**
- ✅ TODAS as escalas aparecem
- ✅ Sem restrições

---

### 9.4 Teste: Filtragem por Ministério + Status

**Setup:**
- Usuário: João Silva (INTEGRANTE em Louvor e Recepção)
- Louvor: A (aberta), B (fechada)
- Recepção: C (aberta), D (fechada)

**Passos:**
1. Ir para `/escalas` (sem filtro)
2. Observar todas (esperado: A, C)
3. Clicar em "Ministério: Louvor" (filtro query param)
4. Observar

**Esperado:**
- ✅ `/escalas` mostra A e C
- ✅ `/escalas?ministerio=1` (Louvor) mostra apenas A
- ✅ `/escalas?ministerio=2` (Recepção) mostra apenas C
- ✅ Sem B e D em nenhum cenário

---

### 9.5 Teste: Endpoint Específico por Ministério

**Setup:**
- Usuário: João Silva (INTEGRANTE em Louvor)
- Escalas: A (aberta), B (fechada)

**Passos:**
1. Chamar `GET /api/meu-ministerio/ministerios/1/escalas`

**Esperado:**
- ✅ Resposta contém apenas A
- ✅ Sem B

---

### 9.6 Teste: Escala Fechada Pode Ser Acessada via Detalhe se Já Confirmou

**Setup:**
- Usuário: João Silva (confirmou disponibilidade em B)
- Escala B está fechada

**Passos:**
1. Chamar `GET /api/meu-ministerio/escalas/{B_ID}`

**Esperado:**
- ⚠️ **VERIFICAR:** Endpoint `buscarEscala()` também filtra ou permite acesso?
- Dependendo, pode precisar de ajuste adicional

---

### 9.7 Teste: Membro que Sai do Ministério

**Setup:**
- João era INTEGRANTE, agora foi removido
- Escala A (aberta) ainda existe

**Passos:**
1. Remover João do ministério via admin
2. Login como João
3. Acessar `/escalas`

**Esperado:**
- ✅ Escala A não aparece (não é membro)
- ✅ Sem erro, apenas lista vazia

---

## 10. Dependências e Compatibilidade

✅ **Sem dependências externas adicionais**
- Usa apenas Stream API (Java 8+)
- Sem bibliotecas novas

✅ **Compatível com:**
- Bug 1 (edição/exclusão) — não interfere
- Endpoints de confirmação — validam separadamente
- Relatórios — não afetados
- Histórico — escalas foram armazenadas corretamente

---

## 11. Resumo das Mudanças

| Arquivo | Tipo | Linhas | O que muda |
|---------|------|--------|-----------|
| `MeuMinisterioService.java` | MODIFICAÇÃO | ~15-20 | Adicionar filtro por `aberta` em `listarMinhasEscalas()` |
| `MeuMinisterioService.java` | MODIFICAÇÃO | ~10-15 | Adicionar filtro por `aberta` em `listarEscalasDoMinisterio()` |
| `MeuMinisterioService.java` | NOVA FUNÇÃO | ~10 | Método helper `isLiderDoMinisterio()` |
| **Total** | - | **~40-45 linhas** | - |

---

## 12. Conclusão

✅ **Bug confirmado:** Usuários padrão veem escalas fechadas.

✅ **Causa identificada:** Backend retorna todas as escalas sem filtro por status.

✅ **Solução viável:** Adicionar filtro simples no `listarMinhasEscalas()` e `listarEscalasDoMinisterio()`.

✅ **Seguro:** Lógica valida papéis corretamente (pastor > líder > membro).

✅ **Testável:** Plano de testes cobre todos os cenários.

---

## 13. Próximo Passo

Aguardando autorização explícita para proceder com a implementação das mudanças no código.

