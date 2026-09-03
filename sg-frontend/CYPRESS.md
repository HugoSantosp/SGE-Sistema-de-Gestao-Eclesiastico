# 🎭 Testes E2E com Cypress - SGE Frontend

Guia completo para rodar e escrever testes E2E no sistema SGE.

---

## 🚀 Início Rápido

### 1. Instalar Dependências
```bash
cd sg-frontend
npm install
```

### 2. Iniciar Servidor Angular
```bash
# Terminal 1
ng serve
```

### 3. Rodar Testes E2E
```bash
# Terminal 2
npm run test:e2e:headless

# Ou abrir interface visual
npm run test:e2e
```

---

## 📁 Estrutura de Pastas

```
cypress/
├── e2e/                          # Testes E2E
│   ├── auth/
│   │   └── login.cy.ts           # Testes de login
│   ├── membros/
│   │   └── membros.cy.ts         # CRUD de membros
│   ├── escala/
│   │   └── escala.cy.ts          # Escala de louvor
│   └── smoke/                    # Testes de smoke (básicos)
│       └── navigation.cy.ts
├── support/
│   ├── e2e.ts                    # Suporte global
│   └── commands.ts               # Comandos customizados
├── fixtures/                     # Dados de teste
│   └── users.json
└── plugins/
    └── index.ts                  # Plugins do Cypress
```

---

## 🧪 Comandos Customizados

O arquivo `cypress/support/commands.ts` define comandos reutilizáveis:

### Login via UI
```typescript
cy.login('admin@icert.local', '123');
```

### Login via API (mais rápido)
```typescript
cy.loginViaApi('admin@icert.local', '123');
```

### Preencher Formulário
```typescript
cy.fillForm({
  'nome': 'João Silva',
  'email': 'joao@email.com',
  'telefone': '(11) 99999-9999'
});
```

### Verificar Toast
```typescript
cy.shouldShowSuccess('Membro criado com sucesso');
cy.shouldShowError('Erro ao salvar');
```

### Buscar na Tabela
```typescript
cy.searchInTable('João');
```

### Editar/Deletar Item
```typescript
cy.editItem('João Silva');
cy.deleteItem('João Silva');
```

---

## 📝 Escrevendo Testes

### Estrutura Básica
```typescript
/// <reference types="cypress" />

describe('Nome do Módulo', () => {
  beforeEach(() => {
    cy.visit('/rota');
  });

  it('deve fazer algo específico', () => {
    // Arrange (Preparar)
    cy.get('#campo').type('valor');
    
    // Act (Ação)
    cy.get('button[type="submit"]').click();
    
    // Assert (Verificar)
    cy.url().should('include', '/resultado');
    cy.contains('Mensagem esperada').should('be.visible');
  });
});
```

### Seletores Recomendados

```typescript
// ✅ Preferidos (mais estáveis)
cy.get('[data-cy="botao-salvar"]');          // Data attributes
cy.get('button[type="submit"]');             // Tipo
cy.contains('Texto do botão');               // Conteúdo

// ⚠️ Evitar (frágeis)
cy.get('.btn-primary');                      // Classes CSS
cy.get('button:nth-child(2)');               // Posição
```

### Dados de Teste

Use fixtures para dados reutilizáveis:
```typescript
// cypress/fixtures/users.json
{
  "admin": {
    "email": "admin@icert.local",
    "password": "123"
  },
  "membro": {
    "nome": "Membro Teste",
    "documento": "12345678901"
  }
}

// No teste:
cy.fixture('users.json').then((users) => {
  cy.login(users.admin.email, users.admin.password);
});
```

---

## 🔧 Configuração

### cypress.config.ts
```typescript
export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    defaultCommandTimeout: 10000,
    env: {
      apiUrl: 'http://localhost:8080',
      adminEmail: 'admin@icert.local',
      adminPassword: '123'
    }
  }
});
```

### Variáveis de Ambiente
Configure em `cypress.config.ts` ou `.env.test`:
- `apiUrl`: URL do backend
- `adminEmail`: Email do administrador
- `adminPassword`: Senha do administrador

---

## 🎯 Fluxos de Teste Críticos

### 1. Login e Autenticação
```typescript
// cypress/e2e/auth/login.cy.ts
describe('Login', () => {
  it('deve fazer login com sucesso', () => {
    cy.visit('/login');
    cy.get('#email').type('admin@icert.local');
    cy.get('#senha').type('123');
    cy.get('button[type="submit"]').click();
    cy.url().should('include', '/dashboard');
  });
});
```

### 2. CRUD de Membros
```typescript
// cypress/e2e/membros/membros.cy.ts
describe('Membros', () => {
  beforeEach(() => {
    cy.loginViaApi('admin@icert.local', '123');
    cy.visit('/membros');
  });

  it('deve cadastrar novo membro', () => {
    cy.contains('Novo').click();
    cy.fillForm({
      'nome': 'João Silva',
      'documento': '12345678901'
    });
    cy.submitForm();
    cy.shouldShowSuccess();
  });
});
```

### 3. Escala de Louvor
```typescript
// cypress/e2e/escala/escala.cy.ts
describe('Escala', () => {
  it('fluxo completo: criar → montar → abrir', () => {
    // Criar escala
    cy.visit('/escala/novo');
    cy.get('#titulo').type('Escala Domingo');
    cy.submitForm();
    
    // Montar
    cy.contains('Escala Domingo')
      .parent()
      .find('.btn-montar')
      .click();
    
    // Adicionar data
    cy.get('#dataEscala').type('2026-12-25');
    cy.get('#horario').type('19:00');
    cy.get('button:contains("Salvar")').click();
    
    // Abrir escala
    cy.visit('/escala');
    cy.contains('Escala Domingo')
      .parent()
      .find('.btn-abrir')
      .click();
    cy.get('.swal2-confirm').click();
  });
});
```

---

## 🐛 Debugging

### Abrir Test Runner
```bash
npm run test:e2e
```

### Console do Cypress
- **F12** no navegador durante teste
- Use `cy.log()` para logar valores
- Use `cy.pause()` para pausar teste

### Timeouts
Aumente se necessário:
```typescript
cy.get('#elemento', { timeout: 15000 }).should('be.visible');
```

### Screenshots e Videos
- Screenshots automáticos em falhas
- Configure video em `cypress.config.ts`:
```typescript
video: true,
```

---

## 📊 Relatórios

### Relatório HTML
```bash
npm run test:e2e:headless
# Gerado em: cypress/reports/index.html
```

### Com Cobertura (requer config extra)
```bash
npm run test:e2e:coverage
```

---

## ✅ Boas Práticas

1. **Use data-cy** para seletores estáveis
2. **Isole testes** - cada teste deve ser independente
3. **Use beforeEach** para setup comum
4. **Evite dependências** entre testes
5. **Teste fluxos críticos** primeiro
6. **Use fixtures** para dados reutilizáveis
7. **Cuide dos timeouts** - aumente se necessário
8. **Pause para debug** - `cy.pause()`
9. **Verifique erros** - `cy.on('uncaught:exception')`
10. **Documente** - comente testes complexos

---

## 🔗 Links Úteis

- [Cypress Docs](https://docs.cypress.io/)
- [Cypress Best Practices](https://docs.cypress.io/guides/references/best-practices)
- [Selector Playground](https://docs.cypress.io/core-concepts/testing-your-app#Selectors)
- [Cypress Examples](https://example.cypress.io/)

---

## 🤝 Contribuindo

Ao adicionar novos testes E2E:

1. Crie pasta em `cypress/e2e/[modulo]/`
2. Nomeie arquivo como `[funcionalidade].cy.ts`
3. Use comandos customizados quando possível
4. Teste fluxos críticos de negócio
5. Adicione no README a nova funcionalidade testada
