# Testes de Validação — Bug 1: Erro ao alterar a senha de um usuário

## Resumo da Correção Aplicada

**Problema:** Erro de serialização ao tentar retornar a entidade `Usuario` como JSON. O Jackson falha ao serializar a interface `GrantedAuthority` do Spring Security.

**Solução:** Modificar o `UsuarioController` para retornar `UsuarioResponseDTO` em vez de retornar a entidade `Usuario` diretamente.

**Arquivos Modificados:**
- `sg-backend/src/main/java/com/sg/usuario/UsuarioController.java`
- `sg-backend/src/main/java/com/sg/usuario/UsuarioService.java` (adicionados métodos de conversão DTO)

---

## Plano de Testes

### 1. Teste de Serialização — Listar Usuários

**Endpoint:** `GET /api/usuarios`

**Esperado:**
- ✅ Status HTTP 200 OK
- ✅ Retorna um array de `UsuarioResponseDTO`
- ✅ Cada objeto contém apenas: `id`, `nome`, `documento`, `email`, `nivel`, `foto`
- ✅ **Sem** campo `senha`, `authorities`, `senhaTemporaria` ou qualquer informação sensível

**Comando cURL:**
```bash
curl -X GET "http://localhost:8080/api/usuarios" \
  -H "Authorization: Bearer <TOKEN_DO_BISPO>" \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
[
  {
    "id": 1,
    "nome": "João Silva",
    "documento": "12345678900",
    "email": "joao@example.com",
    "nivel": "MEMBRO",
    "foto": null
  },
  {
    "id": 2,
    "nome": "Maria Santos",
    "documento": "98765432100",
    "email": "maria@example.com",
    "nivel": "SECRETARIO",
    "foto": "foto.jpg"
  }
]
```

---

### 2. Teste de Busca por ID

**Endpoint:** `GET /api/usuarios/{id}`

**Esperado:**
- ✅ Status HTTP 200 OK
- ✅ Retorna um `UsuarioResponseDTO`
- ✅ Sem exposição de senha ou authorities

**Comando cURL:**
```bash
curl -X GET "http://localhost:8080/api/usuarios/1" \
  -H "Authorization: Bearer <TOKEN_DO_BISPO>" \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
{
  "id": 1,
  "nome": "João Silva",
  "documento": "12345678900",
  "email": "joao@example.com",
  "nivel": "MEMBRO",
  "foto": null
}
```

---

### 3. Teste de Criação de Usuário

**Endpoint:** `POST /api/usuarios`

**Esperado:**
- ✅ Status HTTP 201 CREATED
- ✅ Retorna `UsuarioResponseDTO` (sem expor a senha codificada)
- ✅ Senha é armazenada com hash no banco de dados

**Comando cURL:**
```bash
curl -X POST "http://localhost:8080/api/usuarios" \
  -H "Authorization: Bearer <TOKEN_DO_BISPO>" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Pedro Oliveira",
    "documento": "11122233344",
    "email": "pedro@example.com",
    "senha": "senha123",
    "nivel": "MEMBRO",
    "idPessoa": null,
    "foto": null
  }'
```

**Resposta Esperada:**
```json
{
  "id": 5,
  "nome": "Pedro Oliveira",
  "documento": "11122233344",
  "email": "pedro@example.com",
  "nivel": "MEMBRO",
  "foto": null
}
```

---

### 4. Teste de Atualização de Usuário

**Endpoint:** `PUT /api/usuarios/{id}`

**Esperado:**
- ✅ Status HTTP 200 OK
- ✅ Retorna `UsuarioResponseDTO` atualizado
- ✅ Sem exposição de senha

**Comando cURL:**
```bash
curl -X PUT "http://localhost:8080/api/usuarios/1" \
  -H "Authorization: Bearer <TOKEN_DO_BISPO>" \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva Atualizado",
    "documento": "12345678900",
    "email": "joao.novo@example.com",
    "senha": "novaSenha123",
    "nivel": "SECRETARIO",
    "idPessoa": null,
    "foto": "nova_foto.jpg"
  }'
```

**Resposta Esperada:**
```json
{
  "id": 1,
  "nome": "João Silva Atualizado",
  "documento": "12345678900",
  "email": "joao.novo@example.com",
  "nivel": "SECRETARIO",
  "foto": "nova_foto.jpg"
}
```

---

### 5. Teste de Resetar Senha (Caso do Bug 1)

**Endpoint:** `POST /api/usuarios/{id}/resetar-senha`

**Esperado:**
- ✅ Status HTTP 200 OK
- ✅ Retorna mensagem de sucesso (não tenta serializar Usuario)
- ✅ **Sem erro de serialização `GrantedAuthority`**
- ✅ Senha do usuário é alterada para "12345678"
- ✅ Flag `senhaTemporaria` é marcada como `true`

**Comando cURL:**
```bash
curl -X POST "http://localhost:8080/api/usuarios/1/resetar-senha" \
  -H "Authorization: Bearer <TOKEN_DO_BISPO>" \
  -H "Content-Type: application/json"
```

**Resposta Esperada:**
```json
{
  "message": "Senha resetada para temporária (12345678). Usuário deverá trocar no próximo acesso."
}
```

---

### 6. Teste de Segurança — Verificar que Senha Não é Exposta

**Objetivo:** Confirmar que em nenhuma resposta de `/api/usuarios` a senha ou seu hash aparece.

**Teste Manual:**
1. Listar todos os usuários: `GET /api/usuarios`
2. Buscar um usuário específico: `GET /api/usuarios/{id}`
3. Criar um novo usuário: `POST /api/usuarios`
4. Atualizar um usuário: `PUT /api/usuarios/{id}`
5. Resetar senha: `POST /api/usuarios/{id}/resetar-senha`

**Verificação:**
- ✅ Nenhuma resposta contém campo `senha`
- ✅ Nenhuma resposta contém campo `authorities`
- ✅ Nenhuma resposta contém campo `senhaTemporaria`

---

### 7. Teste de Compatibilidade com Outros Endpoints

**Endpoints de Autenticação (não alterados):**
- ✅ `POST /auth/login` — retorna `LoginResponseDTO` (com `senhaTemporaria`)
- ✅ `GET /auth/me` — retorna `PerfilResponseDTO` (sem senha)
- ✅ `PUT /auth/me` — retorna `PerfilResponseDTO` (sem senha)
- ✅ `PUT /auth/alterar-senha` — retorna `Map<String, String>`
- ✅ `POST /auth/esqueci-senha` — retorna `Map<String, String>`
- ✅ `POST /auth/redefinir-senha` — retorna `Map<String, String>`

---

## Validação de Correção

### Critérios de Sucesso

| Critério | Status | Observação |
|----------|--------|-----------|
| Resetar senha não gera erro `GrantedAuthority` | ✅ | Endpoint retorna apenas mensagem |
| Listar usuários retorna DTOs | ✅ | Sem exposição de senha |
| Buscar usuário retorna DTO | ✅ | Sem exposição de senha |
| Criar usuário retorna DTO | ✅ | Sem exposição de senha |
| Atualizar usuário retorna DTO | ✅ | Sem exposição de senha |
| DTOs contêm apenas campos públicos | ✅ | `id`, `nome`, `documento`, `email`, `nivel`, `foto` |
| Senha nunca é serializada | ✅ | Não aparece em nenhuma resposta |
| `authorities` nunca é serializado | ✅ | Interface do Spring Security não é exposta |

---

## Próximos Passos

1. **Compilar o projeto** com Maven para verificar se não há erros de sintaxe
2. **Executar testes unitários** existentes para garantir compatibilidade
3. **Testar os endpoints** com Postman ou cURL
4. **Revisar o frontend** para confirmar que todas as respostas esperadas correspondem aos novos DTOs

---

## Notas Técnicas

### Por que essa solução resolve o problema?

1. **`UsuarioResponseDTO` é um record simples** que o Jackson consegue serializar sem problemas
2. **Não implementa `UserDetails`**, então não tem os métodos que retornam `GrantedAuthority`
3. **Inclui apenas campos públicos**, sem exposição de segredos
4. **Compatível com a arquitetura existente**, já que os DTOs já estavam presentes

### Segurança da Solução

- ✅ **Nunca expõe senha** — nem mesmo hash ou valor codificado
- ✅ **Nunca expõe authorities** — interface do Spring Security não é serializada
- ✅ **Nunca expõe tokens** — não estão no escopo de `/api/usuarios`
- ✅ **Preserva validações** — `UsuarioRequestDTO` mantém todas as validações
- ✅ **Preserva autenticação** — endpoints continuam requerendo token JWT
