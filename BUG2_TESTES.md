# Plano de Testes — Bug 2: Senha na Edição de Usuário

## Visão Geral

Após as correções do Bug 2, o sistema agora permite:
1. ✅ Editar usuário **sem alterar senha** (deixar campo vazio)
2. ✅ Editar usuário **alterando senha** (digitar nova senha)
3. ✅ Campo de senha não exibe hash (retorna DTO sem senha)
4. ✅ Botão mostrar/ocultar senha para melhor UX

---

## Teste 1: Visualizar Campo Vazio ao Abrir Edição

### Objetivo
Confirmar que o campo de senha não exibe hash ao abrir o formulário de edição.

### Pré-requisitos
- Um usuário existente no sistema (ex: ID 1)
- Acesso administrativo

### Passos

1. Navegar para `/usuarios/editar/1`
2. Esperar carregamento completo
3. Observar o campo "Senha (deixe vazio para manter)"

### Resultado Esperado

- ✅ Campo de senha está **completamente vazio** (não contém caracteres)
- ✅ Não há hash visível (ex: `$2b$10$NWpW7GJl...`)
- ✅ Campo tem `type="password"` por padrão
- ✅ Ícone de olho aparece ao lado do campo

### Evidência

```
Campo de entrada de senha:
┌─────────────────────────────┬──────┐
│ [       ]                   │ 👁️   │ ← Vazio, ícone visível
└─────────────────────────────┴──────┘
Rótulo: "Senha (deixe vazio para manter)"
```

---

## Teste 2: Editar Apenas Nome (Sem Alterar Senha)

### Objetivo
Confirmar que usuário pode editar outros campos sem alterar senha.

### Pré-requisitos
- Um usuário com senha já definida
- Saber a senha atual (para validar depois)

### Passos

1. Navegar para `/usuarios/editar/1`
2. Modificar apenas o campo "Nome": "João Silva" → "João Silva Pereira"
3. **Deixar campo de senha VAZIO**
4. Clicar botão "Atualizar"
5. Esperar confirmação "Registro atualizado com sucesso!"
6. Validar tentando fazer login com a senha antiga

### Resultado Esperado

- ✅ Requisição PUT enviada com os dados modificados
- ✅ Backend **não sobrescreve** a senha
- ✅ Mensagem de sucesso aparece
- ✅ Usuário consegue fazer login com a **senha anterior** (não mudou)
- ✅ Nome foi atualizado: "João Silva Pereira"

### Fluxo de Rede Esperado

```
PUT /api/usuarios/1
{
  "nome": "João Silva Pereira",
  "email": "joao@example.com",
  "documento": "12345678900",
  "senha": null,  ← VAZIO/NULL (não sobrescreve)
  "nivel": "MEMBRO",
  "idPessoa": null,
  "foto": null
}

Resposta:
{
  "id": 1,
  "nome": "João Silva Pereira",
  "email": "joao@example.com",
  "documento": "12345678900",
  "nivel": "MEMBRO",
  "foto": null
}
```

---

## Teste 3: Editar Nome E Alterar Senha

### Objetivo
Confirmar que ao digitar nova senha, o backend codifica e armazena.

### Pré-requisitos
- Mesmo usuário do teste anterior
- Saber a senha antiga (para confirmar que não funciona mais)

### Passos

1. Navegar para `/usuarios/editar/1`
2. Modificar nome: "João Silva Pereira" → "João Santos Silva"
3. Digitar nova senha: "novaSenha123"
4. Clicar botão "Atualizar"
5. Esperar confirmação
6. **Validar:**
   - Tentar login com senha nova: **SUCESSO**
   - Tentar login com senha anterior: **FALHA**

### Resultado Esperado

- ✅ Requisição PUT com `"senha": "novaSenha123"`
- ✅ Backend codifica a senha
- ✅ Mensagem de sucesso
- ✅ Nome foi atualizado
- ✅ **Login com nova senha:** Funciona
- ✅ **Login com senha anterior:** Não funciona
- ✅ Flag `senhaTemporaria` marca como `false`

### Fluxo de Rede Esperado

```
PUT /api/usuarios/1
{
  "nome": "João Santos Silva",
  "email": "joao@example.com",
  "documento": "12345678900",
  "senha": "novaSenha123",  ← NOVA SENHA
  "nivel": "MEMBRO",
  "idPessoa": null,
  "foto": null
}

Resposta:
{
  "id": 1,
  "nome": "João Santos Silva",
  ...
}
```

---

## Teste 4: Botão Mostrar/Ocultar Senha (Toggle)

### Objetivo
Confirmar que o ícone de olho alterna entre mostrar e ocultar.

### Pré-requisitos
- Estar no formulário de edição

### Passos

1. Navegar para `/usuarios/editar/1`
2. Digitar algo no campo de senha: "test123"
3. Observar campo: exibe `●●●●●●●` (oculto)
4. Clicar no ícone de olho (👁️)
5. Observar: texto muda para "test123" (visível)
6. Clicar novamente no ícone
7. Observar: volta para `●●●●●●●` (oculto)

### Resultado Esperado

- ✅ **Estado 1 (padrão):** Campo com `type="password"`, ícone é 👁️ (olho aberto)
- ✅ **Após 1º clique:** Campo com `type="text"`, ícone é 👁️‍🗨️ (olho fechado)
- ✅ **Após 2º clique:** Volta para `type="password"`, ícone é 👁️
- ✅ Comportamento é consistente e fluido

### Evidência Visual

```
Antes do clique (padrão):
┌─────────────────────────────┬──────┐
│ [●●●●●●●]   type=password  │ 👁️   │
└─────────────────────────────┴──────┘

Após clicar o botão:
┌─────────────────────────────┬──────┐
│ [test123]    type=text      │ 👁️‍🗨️ │
└─────────────────────────────┴──────┘
```

---

## Teste 5: Criar Novo Usuário Sem Senha (Validação)

### Objetivo
Confirmar que criar novo usuário **exige** senha.

### Pré-requisitos
- Nenhum, começar do zero

### Passos

1. Navegar para `/usuarios/novo`
2. Preencher campos obrigatórios:
   - Nome: "Maria Silva"
   - Email: "maria@example.com"
   - Documento: "98765432100"
   - Nível: "MEMBRO"
3. **Deixar campo de senha VAZIO**
4. Clicar "Salvar"

### Resultado Esperado

- ❌ Erro de validação aparece
- ❌ Mensagem: "Senha deve ter no mínimo 3 caracteres (deixe vazio para manter)"
- ❌ Usuário **não consegue** salvar
- ✅ Campo de senha é destacado em vermelho

### Fluxo Esperado

```
Validação no frontend:
- Senha vazia = inválido (campo obrigatório na criação)

Se passar validação frontend, backend também valida:
- Senha < 3 caracteres = erro 422
- Mensagem clara sobre tamanho mínimo
```

---

## Teste 6: Criar Novo Usuário Com Senha

### Objetivo
Confirmar que criar novo usuário funciona quando senha é fornecida.

### Pré-requisitos
- Nenhum

### Passos

1. Navegar para `/usuarios/novo`
2. Preencher todos os campos:
   - Nome: "Maria Silva"
   - Email: "maria@example.com"
   - Documento: "98765432100"
   - Nível: "MEMBRO"
   - Senha: "abc123" (mínimo 3 caracteres)
3. Clicar "Salvar"

### Resultado Esperado

- ✅ Usuário criado com sucesso
- ✅ Mensagem: "Registro criado com sucesso!"
- ✅ Redirecionado para listagem `/usuarios`
- ✅ Novo usuário aparece na lista
- ✅ Ao tentar fazer login:
  - Email: maria@example.com
  - Senha: abc123
  - **SUCESSO** — usuário faz login
- ✅ Backend marcou `senhaTemporaria = false` (pois recebeu senha no POST)

### Fluxo de Rede Esperado

```
POST /api/usuarios
{
  "nome": "Maria Silva",
  "email": "maria@example.com",
  "documento": "98765432100",
  "senha": "abc123",
  "nivel": "MEMBRO",
  "idPessoa": null,
  "foto": null
}

Resposta:
{
  "id": 5,
  "nome": "Maria Silva",
  "email": "maria@example.com",
  "documento": "98765432100",
  "nivel": "MEMBRO",
  "foto": null
}

No banco de dados:
- senha: $2b$10$... (hash)
- senhaTemporaria: false
```

---

## Teste 7: Resetar Senha Continua Funcionando

### Objetivo
Confirmar que o botão "Resetar Senha" não foi afetado pela correção.

### Pré-requisitos
- Um usuário criado com qualquer senha

### Passos

1. Navegar para `/usuarios` (listagem)
2. Encontrar o usuário
3. Clicar botão "Resetar Senha" (ícone 🔄 ou similar)
4. Confirmar a ação
5. Validar:
   - Tentar login com email + senha "12345678"
   - **SUCESSO** — login funciona

### Resultado Esperado

- ✅ Mensagem de confirmação aparece
- ✅ Após confirmação: "Senha resetada para temporária (12345678)"
- ✅ Na próxima edição do usuário:
  - Campo de senha está vazio (normal)
  - Mas backend marca `senhaTemporaria = true`
  - Na tela de perfil mostra banner: "🔐 Senha Temporária"
- ✅ Login com "12345678" funciona
- ✅ Usuário é obrigado a trocar no próximo login

---

## Teste 8: Senha Temporária Força Mudança

### Objetivo
Confirmar que usuário com senha temporária é forçado a alterar.

### Pré-requisitos
- Um usuário com `senhaTemporaria = true`

### Passos

1. **Fazer login** com o usuário:
   - Email: joao@example.com
   - Senha: 12345678
2. **SUCESSO** — login realizado
3. Observar tela de perfil (`/perfil`)
4. Verificar se há banner: "🔐 Senha Temporária"
5. Clicar botão "Alterar Senha"
6. Digitar nova senha
7. Submeter

### Resultado Esperado

- ✅ Login funciona (não bloqueia entrada)
- ✅ Banner aparece na tela de perfil
- ✅ Botão "Alterar Senha" acessível
- ✅ Após alterar:
  - Banner desaparece
  - `senhaTemporaria` marca como `false`
  - Próxima edição não mostra banner

---

## Teste 9: Validação de Tamanho Mínimo de Senha

### Objetivo
Confirmar que senha com menos de 3 caracteres é rejeitada.

### Pré-requisitos
- Estar no formulário de edição

### Passos

1. Navegar para `/usuarios/editar/1`
2. Digitar senha curta: "ab" (2 caracteres)
3. Clicar "Atualizar"

### Resultado Esperado

- ❌ Erro de validação
- ❌ Mensagem: "Senha deve ter no mínimo 3 caracteres (deixe vazio para manter)"
- ❌ Botão "Atualizar" desabilitado ou erro aparecer

### Cenários Adicionais

| Entrada | Resultado | Motivo |
|---------|-----------|--------|
| (vazio) | ✅ Válido | Permite não alterar |
| "ab" | ❌ Inválido | < 3 caracteres |
| "abc" | ✅ Válido | = 3 caracteres (mínimo) |
| "password123" | ✅ Válido | Normal |

---

## Teste 10: Campo Senha Não Expõe Hash em Qualquer Cenário

### Objetivo
Confirmar que a senha codificada **nunca** aparece em nenhuma resposta da API.

### Pré-requisitos
- Vários usuários no sistema

### Passos

1. **GET `/api/usuarios`** — Listar todos
   - Verificar JSON: Nenhum campo `senha`
   - ✅ Esperado
   
2. **GET `/api/usuarios/1`** — Buscar um específico
   - Verificar JSON: Nenhum campo `senha`
   - ✅ Esperado

3. **PUT `/api/usuarios/1`** — Atualizar
   - Enviar: `{ "nome": "João", ..., "senha": null }`
   - Resposta: Nenhum campo `senha`
   - ✅ Esperado

4. **POST `/api/usuarios`** — Criar
   - Enviar: `{ "nome": "Maria", ..., "senha": "abc123" }`
   - Resposta: Nenhum campo `senha`
   - ✅ Esperado

5. **POST `/api/usuarios/1/resetar-senha`** — Resetar
   - Resposta: `{ "message": "..." }`
   - Sem campo `senha` ou hash
   - ✅ Esperado

### Resultado Esperado

- ✅ Em **nenhuma** resposta de `/api/usuarios/**` aparece o campo `senha`
- ✅ Em **nenhuma** resposta aparece hash (ex: `$2b$10$...`)
- ✅ Segurança mantida: senha nunca é exposta via API

---

## Teste 11: Compatibilidade com Módulo "Meu Ministério"

### Objetivo
Confirmar que a mudança não afeta o módulo de perfil pessoal.

### Pré-requisitos
- Estar logado como usuário comum

### Passos

1. Navegar para `/perfil` (seu perfil pessoal)
2. Clicar "Alterar Senha"
3. Preencher:
   - Senha atual: (sua senha)
   - Nova senha: "newPass123"
   - Confirmação: "newPass123"
4. Submeter

### Resultado Esperado

- ✅ Formulário de alterar senha funciona normalmente
- ✅ Não é afetado pela correção (usa endpoint diferente)
- ✅ Validações de senha continuam
- ✅ Mensagem de sucesso aparece

---

## Teste 12: Bulk Testing — Fluxo Completo

### Objetivo
Testar todo o fluxo de gestão de usuários.

### Passos

1. **Criar novo usuário:** "Pedro" com senha "pedro123"
2. **Editar (sem alterar senha):** Mudar nome para "Pedro Silva"
3. **Resetar senha:** Via botão administrativo
4. **Editar (com nova senha):** Digitar "novaSenha456"
5. **Validar login:** Com "novaSenha456" funciona, anterior não
6. **Verificar perfil:** Sem campo de senha exposto

### Resultado Esperado

- ✅ Todos os passos funcionam
- ✅ Sem erros inesperados
- ✅ Senha não é exposta em nenhum ponto
- ✅ Campo de senha sempre vazio ao abrir

---

## Checklist de Testes

| # | Teste | Status | Obs |
|---|-------|--------|-----|
| 1 | Campo vazio ao abrir | ⏳ | Sem hash visível |
| 2 | Editar sem alterar | ⏳ | Senha mantém |
| 3 | Editar E alterar | ⏳ | Senha muda |
| 4 | Toggle mostrar/ocultar | ⏳ | Ícone funciona |
| 5 | Criar sem senha | ⏳ | Erro de validação |
| 6 | Criar com senha | ⏳ | Login funciona |
| 7 | Resetar senha | ⏳ | "12345678" funciona |
| 8 | Senha temporária | ⏳ | Banner aparece |
| 9 | Validação tamanho | ⏳ | < 3 caracteres = erro |
| 10 | Sem exposição | ⏳ | Nunca na resposta API |
| 11 | Compatibilidade | ⏳ | Módulo Meu Ministério ok |
| 12 | Fluxo completo | ⏳ | Tudo integrado |

---

## Relatório de Testes

**Após completar os testes**, preencher:

```
Data: _____
Testador: _____
Ambiente: DEV / STAGING / PROD

Resultado Geral:
- [ ] Todos os 12 testes passaram ✅
- [ ] Alguns testes falharam ❌
- [ ] Bloqueadores encontrados ⚠️

Problemas Encontrados:
1. ...
2. ...

Observações:
...
```

---

## Conclusão

Após passar em todos os 12 testes, o Bug 2 está **completamente resolvido** e pronto para produção.

✅ Segurança: Senha nunca é exposta
✅ Funcionalidade: Editar sem alterar funciona
✅ UX: Botão mostrar/ocultar adiciona conforto
✅ Compatibilidade: Sem quebra de nada existente
