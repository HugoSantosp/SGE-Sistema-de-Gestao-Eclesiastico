# 🧪 Testes do SGE - Documentação

Este documento descreve como rodar os testes unitários, de integração e E2E do sistema SGE.

---

## 📋 Pré-requisitos

### Backend
- Java 17+
- Maven 3.8+
- Docker (para testes de integração com TestContainers)

### Frontend
- Node.js 18+
- npm 9+
- Chrome/Chromium (para testes E2E)
- Angular CLI (`npm install -g @angular/cli`)

---

## 🎯 Visão Geral dos Testes

| Tipo | Ferramenta | Cobertura | Comando |
|------|------------|-----------|---------|
| **Unitários (Backend)** | JUnit 5 + Mockito | Lógica de negócio | `mvn test` |
| **Integração (Backend)** | TestContainers + PostgreSQL | Endpoints + BD | `mvn test -Dtest="*IntegrationTest"` |
| **Unitários (Frontend)** | Jasmine + Karma | Componentes Angular | `ng test` |
| **E2E (Frontend)** | Cypress | Fluxos completos | `cypress run` |

---

## 🚀 Rodando os Testes

### Backend (SGE-Backend)

#### Testes Unitários
```bash
cd sg-backend

# Rodar todos os testes unitários
mvn test

# Ou usando o script
./scripts/run-tests.sh unit
```

#### Testes de Integração (requer Docker)
```bash
cd sg-backend

# Rodar testes de integração
mvn test -Dtest="*IntegrationTest"

# Ou usando o script
./scripts/run-tests.sh integration
```

#### Todos os Testes + Cobertura
```bash
cd sg-backend

# Rodar com relatório de cobertura (JaCoCo)
./scripts/run-tests.sh coverage

# Ou manualmente
mvn clean test jacoco:report
```

**Relatório de cobertura:** `site/jacoco/index.html`

---

### Frontend (SGE-Frontend)

#### Testes Unitários (Karma)
```bash
cd sg-frontend

# Instalar dependências
npm install

# Rodar testes unitários
ng test

# Ou com cobertura
ng test --code-coverage
```

#### Testes E2E (Cypress)

**Pré-requisito:** O servidor Angular deve estar rodando na porta 4200.

```bash
# Terminal 1: Iniciar servidor
ng serve

# Terminal 2: Rodar testes
cd sg-frontend
npm run test:e2e:headless
```

**Abrir Cypress Test Runner (interface visual):**
```bash
npm run test:e2e
```

#### Todos os Testes
```bash
cd sg-frontend

# Rodar todos os testes (unitários + E2E)
npm run test:all

# Ou com cobertura completa
npm run test:ci
```

---

## 📊 Cobertura de Código

### Backend (JaCoCo)

O relatório é gerado automaticamente ao rodar:
```bash
mvn clean test jacoco:report
```

**Localização do relatório:** `sg-backend/site/jacoco/index.html`

**Configuração:** O plugin JaCoCo está configurado no `pom.xml` e gera relatórios em XML e HTML.

### Frontend (Istanbul/Angular)

Para gerar relatório de cobertura do Angular:
```bash
ng test --code-coverage
```

**Localização do relatório:** `sg-frontend/coverage/sg-frontend/index.html`

**Configuração:** Adicione no `angular.json` dentro do target `test`:
```json
"codeCoverage": true,
"codeCoverageExclude": [
  "src/**/*.spec.ts",
  "src/test.ts"
]
```

---

## 🔧 Configurações Importantes

### Backend - application-test.yml

Os testes usam o profile `test` que configura:
- Banco H2 em memória (para unitários)
- TestContainers PostgreSQL (para integração)
- JWT secret para testes
- Flyway desabilitado

### Frontend - cypress.config.ts

Configuração do Cypress:
- **baseUrl:** `http://localhost:4200`
- **Timeout:** 10 segundos
- **Retry:** 2 tentativas em modo CI

### Variáveis de Ambiente para Cypress

Configure em `cypress.config.ts`:
```typescript
env: {
  apiUrl: 'http://localhost:8080',
  adminEmail: 'admin@icert.local',
  adminPassword: '123'
}
```

---

## 📁 Estrutura de Testes

```
sg-backend/
├── src/test/java/com/sg/
│   ├── auth/AuthServiceTest.java           # Testes unitários de Auth
│   ├── usuario/UsuarioServiceTest.java     # Testes unitários de Usuários
│   ├── integration/                         # Testes de integração
│   │   ├── IntegrationTestBase.java         # Classe base (TestContainers)
│   │   ├── AuthIntegrationTest.java         # Integração Auth
│   │   ├── UsuarioIntegrationTest.java      # Integração Usuários
│   │   └── EscalaIntegrationTest.java       # Integração Escalas
│   └── ...
└── scripts/run-tests.sh                     # Script auxiliar

sg-frontend/
├── cypress/
│   ├── e2e/                                 # Testes E2E
│   │   ├── auth/login.cy.ts                 # Login
│   │   ├── membros/membros.cy.ts            # CRUD Membros
│   │   └── escala/escala.cy.ts              # Escala de Louvor
│   └── support/
│       ├── e2e.ts                           # Suporte global
│       └── commands.ts                      # Comandos customizados
├── cypress.config.ts                        # Configuração Cypress
└── scripts/run-tests.sh                     # Script auxiliar
```

---

## 🐛 Troubleshooting

### Erros Comuns

#### "Docker not running" (TestContainers)
```bash
# Inicie o Docker Desktop
# Ou verifique se está rodando
docker info
```

#### "Port 4200 already in use" (Cypress)
```bash
# Mата processo na porta
lsof -ti:4200 | xargs kill -9

# Ou use porta diferente
ng serve --port 4201
```

#### "Chrome not found" (Karma)
```bash
# Instale o Chrome ou use ChromeHeadless
ng test --browsers=ChromeHeadless
```

#### Timeout nos testes E2E
Aumente o timeout em `cypress.config.ts`:
```typescript
defaultCommandTimeout: 15000,
```

---

## ✅ Boas Práticas

1. **Antes de commitar:** Rode `npm run test:all` no frontend e `mvn test` no backend
2. **Testes de integração:** Rode apenas quando necessário (requer Docker)
3. **Cobertura mínima:** Mantenha acima de 70% para código novo
4. **Testes E2E:** Foque nos fluxos críticos (login, CRUD principal, escala)
5. **Flaky tests:** Se um teste falhar intermitentemente, adicione retry ou corrija a causa raiz

---

## 📈 Métricas de Teste

Para monitorar a qualidade, execute periodicamente:

```bash
# Backend - relatório completo
cd sg-backend
./scripts/run-tests.sh coverage

# Frontend - relatório completo
cd sg-frontend
npm run test:ci
```

**Métricas importantes:**
- Cobertura de código (mínimo 70%)
- Taxa de sucesso dos testes (mínimo 95%)
- Tempo de execução dos testes

---

## 🤝 Contribuindo com Testes

Ao adicionar nova funcionalidade:

1. **Backend:** Crie testes unitários no mesmo pacote da classe
2. **Frontend:** Crie testes unitários para componentes (.spec.ts)
3. **E2E:** Adicione testes para fluxos críticos de negócio
4. **Integração:** Teste endpoints que usam banco de dados

**Exemplo de commit:**
```
feat(membros): adicionar validação de CPF

- Adicionado teste unitário para validação
- Adicionado teste de integração para criação
- Adicionado teste E2E para formulário
```

---

## 📚 Referências

- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [TestContainers](https://www.testcontainers.org/)
- [Cypress Documentation](https://docs.cypress.io/)
- [JaCoCo](https://www.jacoco.org/)
- [Angular Testing](https://angular.io/guide/testing)

---

**Dúvidas?** Consulte a documentação oficial ou abra uma issue no repositório.
