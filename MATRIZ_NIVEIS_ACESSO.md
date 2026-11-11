# Matriz de Níveis de Acesso — SGE ICERT (v4 — implementada)

> **Implementado** em 10/11/2026. Este documento agora reflete o código.
> v3 aprovada por você; v4 registra a implementação e os dois ajustes técnicos necessários para o Meu Ministério continuar funcionando.
> Base: branch `master`, commit `0329fd6` + alterações locais.

---

## 1. Perfis

| Código | Nome | Papel |
|---|---|---|
| `PASTOR_PRESIDENTE` | Pastor Presidente | Perfil mais alto, super admin. |
| `PASTOR_AUXILIAR` | Pastor Auxiliar | Quase o mesmo acesso do Presidente; Financeiro apenas leitura. |
| `TESOUREIRO` | Tesoureiro | Foco financeiro. |
| `SECRETARIO` | Secretário | Foco em pessoas, eventos e tarefas. |
| `MEMBRO` | Membro | **Sem acesso ao admin.** Só Meu Ministério, e apenas se estiver vinculado a um ministério. |

- Um perfil por usuário: `usuario.nivel` (`@Enumerated(EnumType.STRING)`), sem múltiplos papéis.
- `MEMBRO` falta no enum TypeScript (`sge.models.ts`) — será incluído.

---

## 2. Situação atual do controle

| Camada | Onde | Hoje |
|---|---|---|
| Backend — rotas | `SecurityConfig.java:82-108` | Só separa público de autenticado. `hasRole("ADMIN")` nunca é satisfeito. |
| Backend — método | `@EnableMethodSecurity` ligado, zero `@PreAuthorize` | Nenhuma regra por perfil. |
| Backend — service | `MeuMinisterioService.isAcessoAmpliado` | Única regra real (pastores veem tudo). |
| Frontend — rota | `RoleGuard` só em `/usuarios` e nos 4 dashboards | Demais telas dependem do menu. |
| Frontend — menu | `sidebar.component.ts:30-68` | Proteção efetiva hoje. |

Qualquer perfil autenticado — inclusive `MEMBRO` — consegue chamar a API para criar usuários, lançar despesas ou alterar configurações.

---

## 3. Matriz de endpoints

**T** = ler e escrever · **L** = somente leitura · **—** = sem acesso

### 3.1 Pessoas e cadastros

| Endpoint | PP | PA | TES | SEC | Regra |
|---|:--:|:--:|:--:|:--:|---|
| `/api/usuarios` | T | T | — | — | Criar/editar usuários e resetar senha. |
| `/api/membros` | T | T | — | T | Leitura também liberada a `MEMBRO` (Meu Ministério). |
| `/api/bispos` | T | T | — | — | |
| `/api/presbiteros` | T | T | — | — | |
| `/api/tesoureiros` | T | T | L | — | Tesoureiro consulta o colegiado. |
| `/api/secretarios` | T | T | — | L | |
| `/api/cargos` | T | T | — | T | Leitura usada em formulários de pessoas. Secretário cadastra, edita e exclui. |
| `/api/ministerios` | T | T | L | **T** | Secretário cria e edita ministérios. |
| `/api/celulas` | T | T | — | **T** | Secretário cria e edita células. |
| `/api/igrejas` | T | T | L | L | |
| `/api/fornecedores` | T | T | T | — | Tesoureiro mantém fornecedores. |
| `/api/profissionais` (Mural) | T | T | — | **T** | Secretário cria e edita o mural. |

### 3.2 Operação

| Endpoint | PP | PA | TES | SEC | Regra |
|---|:--:|:--:|:--:|:--:|---|
| `/api/eventos` | T | T | L | T | |
| `/api/tarefas` | T | T | — | T | |
| `/api/notificacoes` | T | T | L | L | Leitura das próprias notificações. |
| `/api/escalas` | T | T | L | L | Leitura também liberada a `MEMBRO` (instrumentos no Meu Ministério). |
| `/api/meu-ministerio` | T | T | T | T | Filtro por perfil já existe no service; **sem mudança**. |
| `/api/config` | T | T | L | L | Escrita sensível: renomear `nome` quebra a busca pública. |
| `/api/financeiro` | T | **L** | T | — | Único ponto onde o Auxiliar perde escrita. |
| `/api/reports` e `/api/reports/download` | T | T | T | L | |
| `/api/upload` | T | T | T | T | Upload de foto de perfil. |
| `/api/uploads/**` | público | | | | Arquivos com URL assinada. |
| `/api/admin/**` | T | T | — | — | Trocar `hasRole("ADMIN")` por `PASTOR_PRESIDENTE`/`PASTOR_AUXILIAR`. Sem controller hoje. |
| `/actuator/**` (exceto health) | T | — | — | — | Só Presidente. |

### 3.3 Público (sem autenticação, inalterado)

`/auth/login`, `/auth/esqueci-senha`, `/auth/redefinir-senha`, `/api/public/**`, `/api/publico/escala/**`, `/api/uploads/**`, Swagger, `/actuator/health`.

---

## 4. Matriz de telas do admin

Todas as rotas internas passam a ter `RoleGuard` além do `AuthGuard`.

| Rota | PP | PA | TES | SEC |
|---|:--:|:--:|:--:|:--:|
| `/dashboard` (presidente) | ✔ | — | — | — |
| `/dashboard/pastor` | — | ✔ | — | — |
| `/dashboard/tesoureiro` | — | — | ✔ | — |
| `/dashboard/secretario` | — | — | — | ✔ |
| `/perfil` | ✔ | ✔ | ✔ | ✔ |
| `/usuarios` | ✔ | ✔ | — | — |
| `/bispos` | ✔ | ✔ | — | — |
| `/presbiteros` | ✔ | ✔ | — | — |
| `/tesoureiros` | ✔ | ✔ | ✔ | — |
| `/secretarios` | ✔ | ✔ | — | ✔ |
| `/membros` | ✔ | ✔ | — | ✔ |
| `/cargos` | ✔ | ✔ | — | ✔ |
| `/admin/ministerios` | ✔ | ✔ | ✔ | ✔ |
| `/admin/celulas` | ✔ | ✔ | — | ✔ |
| `/admin/profissionais` | ✔ | ✔ | — | ✔ |
| `/fornecedores` | ✔ | ✔ | ✔ | — |
| `/financeiro/pagar` e `/receber` | ✔ | **L** | ✔ | — |
| `/eventos` | ✔ | ✔ | ✔ | ✔ |
| `/tarefas` | ✔ | ✔ | — | ✔ |
| `/notificacoes` | ✔ | ✔ | ✔ | ✔ |
| `/configuracoes` | ✔ | ✔ | — | — |
| `/igrejas` | ✔ | ✔ | ✔ | ✔ |

`MEMBRO` não aparece: sem acesso a nenhuma rota do admin.

---

## 5. Meu Ministério — sem mudança

Permanece exatamente como está hoje. A distinção continua em `MeuMinisterioService` (`isAcessoAmpliado`, `isLiderDoMinisterio`, `validarLider`, `validarMembro`) e nos componentes (`isPastor`).

| Recurso | PP | PA | Líder | MEM vinculado |
|---|:--:|:--:|:--:|:--:|
| Ver todos os ministérios | ✔ | ✔ | — | — |
| Ver o próprio ministério | ✔ | ✔ | ✔ | ✔ |
| Gerenciar escalas do próprio ministério | ✔ | ✔ | ✔ | — |
| Escala genérica (sem ministério) | ✔ | ✔ | — | — |
| Confirmar/recusar própria designação | ✔ | ✔ | ✔ | ✔ |
| Editar membros do ministério | ✔ | ✔ | ✔ | — |

### 5.1 Membro sem vínculo

Regra nova, apenas de bloqueio de entrada:

- `MEMBRO` sem vínculo a nenhum ministério **não acessa nada**.
- Ao entrar no Meu Ministério, recebe uma tela de aviso ("Você ainda não está vinculado a um ministério. Procure a secretaria.") e nenhuma chamada de dados é feita.
- No login do admin, `MEMBRO` é redirecionado para o Meu Ministério, não para `/dashboard`.

---

## 6. Site público

Sem autenticação. Consome `/api/public/**` e `/api/publico/escala/**`. Nenhuma mudança.

---

## 7. Escopo da implementação

1. `SecurityConfig`: `hasRole("ADMIN")` → `hasAnyRole("PASTOR_PRESIDENTE","PASTOR_AUXILIAR")` em `/api/admin/**`; `/actuator/**` só Presidente.
2. `@PreAuthorize` por controller conforme seções 3.1 e 3.2, separando leitura de escrita. Financeiro: escrita só PP e TES; PA com `hasAnyRole` de leitura.
3. `RoleGuard` nas rotas do admin conforme seção 4, incluindo `/admin/celulas`, `/admin/ministerios` e `/admin/profissionais` para `SECRETARIO`.
4. `MEMBRO` no enum TypeScript e em `getDashboardRoute()` → redireciona ao Meu Ministério.
5. Bloqueio de `MEMBRO` sem vínculo no Meu Ministério (seção 5.1).
6. Testes `@WebMvcTest`/MockMvc por perfil em `usuarios`, `financeiro`, `config`.

**Fora de escopo:** múltiplos perfis por usuário, permissões por registro, auditoria de acessos, mudanças no Meu Ministério além do bloqueio da seção 5.1.

---

## 8. Decisões confirmadas

| # | Pergunta | Resposta |
|---|---|---|
| 1 | `PASTOR_AUXILIAR` em `/api/usuarios` | Acesso total |
| 2 | `PASTOR_AUXILIAR` em `/api/bispos` | Acesso total |
| 3 | `PASTOR_AUXILIAR` em `/api/config` | Acesso total; Financeiro somente leitura |
| 4 | `SECRETARIO` em eventos e tarefas | Leitura e escrita |
| 5 | `SECRETARIO` em células, ministérios e mural | Leitura e escrita |

---

## 9. O que foi implementado

### Backend

| Arquivo | Mudança |
|---|---|
| `shared/config/SecurityConfig.java` | `/actuator/**` → `hasRole("PASTOR_PRESIDENTE")`; `/api/admin/**` → `hasAnyRole("PASTOR_PRESIDENTE","PASTOR_AUXILIAR")`. |
| 20 controllers | 113 `@PreAuthorize` inseridos, separando `@GetMapping` (leitura) de `@Post/Put/Patch/DeleteMapping` (escrita). |

Controllers anotados: `Usuario`, `Financeiro`, `Configuracao`, `Bispo`, `Presbitero`, `Tesoureiro`, `Secretario`, `Cargo`, `Ministerio`, `Celula`, `Igreja`, `Fornecedor`, `Profissional`, `Evento`, `Tarefa`, `Notificacao`, `Escala`, `Membro`, `Report`, `ReportDownload`.

Sem anotação (por decisão): `AuthController`, `PublicController`, `EscalaPublicaController`, `MeuMinisterioController` (filtro no service), `FileUploadController` e `UploadResourceController`.

### Frontend

| Arquivo | Mudança |
|---|---|
| `app-routing.module.ts` | `RoleGuard` + `data.roles` em 15 rotas; nova rota `/sem-acesso`. |
| `app-mobile-routing.module.ts` | `/usuarios` passa a aceitar `PASTOR_AUXILIAR`. |
| `features/usuarios/usuarios.module.ts` | 3 ocorrências de `roles` atualizadas. |
| `core/auth/auth.service.ts` | `getDashboardRoute()` → `MEMBRO` vai para `/sem-acesso`. |
| `core/models/sge.models.ts` | `MEMBRO` adicionado ao enum. |
| `core/components/sidebar/sidebar.component.ts` | Menu alinhado à matriz; Dashboard e Meu Perfil com `roles`. |
| `features/sem-acesso/sem-acesso.component.ts` | Novo componente standalone com aviso e link para o Meu Ministério. |
| `meu-ministerio/.../ministerios.component.ts/.html` | `semVinculo` quando membro não tem ministério; aba Escalas oculta nesse caso. |

### Testes

| Arquivo | Cobertura |
|---|---|
| `src/test/java/com/sg/security/AccessControlMatrixTest.java` | 18 casos com `@SpringBootTest` + `@AutoConfigureMockMvc` + perfil `test` (H2): sem token, Presidente, Auxiliar, Tesoureiro, Secretário e Membro em `/api/usuarios`, `/api/financeiro`, `/api/config`, `/api/membros` e `/api/eventos`. |

### Verificação

- `tsc -p tsconfig.app.json --noEmit` → 0
- `tsc -p tsconfig.site.json --noEmit` → 0
- `tsc -p projects/meu-ministerio/tsconfig.app.json --noEmit` → 0
- `javac` nos controllers alterados e no teste: nenhum erro de sintaxe (só símbolos ausentes por falta de classpath).
- **Testes não executados:** não há Maven nem repositório local (`~/.m2`) neste ambiente. Rode na sua máquina:
  `mvn test -Dtest=AccessControlMatrixTest`
- **Não** houve build completo nem teste em navegador. O backend precisa ser recompilado e reiniciado para valer.
