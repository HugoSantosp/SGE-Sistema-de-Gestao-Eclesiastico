// ***********************************************************
// Comandos customizados do Cypress para o SGE
// ***********************************************************

// Comando para login
Cypress.Commands.add('login', (email: string, password: string) => {
  cy.visit('/login');
  cy.get('input[name="email"], input[type="email"], #email').type(email);
  cy.get('input[name="senha"], input[type="password"], #senha').type(password);
  cy.get('button[type="submit"]').click();
  
  // Aguarda redirecionamento para o dashboard
  cy.url().should('include', '/dashboard');
});

// Comando para login via API (mais rápido para testes)
Cypress.Commands.add('loginViaApi', (email: string, password: string) => {
  cy.request({
    method: 'POST',
    url: `${Cypress.env('apiUrl')}/auth/login`,
    body: {
      user: email,
      senha: password
    }
  }).then((response) => {
    expect(response.status).to.eq(200);
    const { token, nivel } = response.body;
    
    // Salva o token no localStorage
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(response.body));
    
    // Intercepta requests futuros para incluir o token
    cy.intercept('**/*', (req) => {
      req.headers['Authorization'] = `Bearer ${token}`;
    });
  });
});

// Comando para verificar se está na página correta
Cypress.Commands.add('shouldBeOnPage', (page: string) => {
  cy.url().should('include', page);
});

// Comando para aguardar carregamento
Cypress.Commands.add('waitForLoad', () => {
  cy.get('.spinner-border, .loading, [data-loading]').should('not.exist');
});

// Comando para preencher formulário
Cypress.Commands.add('fillForm', (fields: Record<string, string>) => {
  Object.entries(fields).forEach(([field, value]) => {
    cy.get(`[name="${field}"], #${field}`).clear().type(value);
  });
});

// Comando para submeter formulário
Cypress.Commands.add('submitForm', () => {
  cy.get('button[type="submit"]').click();
  cy.waitForLoad();
});

// Comando para verificar toast de sucesso
Cypress.Commands.add('shouldShowSuccess', (message?: string) => {
  cy.get('.toast-success, .alert-success, [data-cy="success-toast"]')
    .should('be.visible');
  if (message) {
    cy.contains(message).should('be.visible');
  }
});

// Comando para verificar toast de erro
Cypress.Commands.add('shouldShowError', (message?: string) => {
  cy.get('.toast-error, .alert-danger, [data-cy="error-toast"]')
    .should('be.visible');
  if (message) {
    cy.contains(message).should('be.visible');
  }
});

// Comando para deletar item
Cypress.Commands.add('deleteItem', (itemName: string) => {
  cy.contains(itemName).parent('tr').find('button[title="Excluir"], .btn-delete').click();
  cy.get('.swal2-confirm, button:contains("Confirmar")').click();
});

// Comando para editar item
Cypress.Commands.add('editItem', (itemName: string) => {
  cy.contains(itemName).parent('tr').find('button[title="Editar"], .btn-edit').click();
});

// Comando para buscar na tabela
Cypress.Commands.add('searchInTable', (searchTerm: string) => {
  cy.get('input[type="search"], input[placeholder*="Buscar"]').clear().type(searchTerm);
  cy.waitForLoad();
});

// Declaração de tipos para os comandos customizados
declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>;
      loginViaApi(email: string, password: string): Chainable<void>;
      shouldBeOnPage(page: string): Chainable<void>;
      waitForLoad(): Chainable<void>;
      fillForm(fields: Record<string, string>): Chainable<void>;
      submitForm(): Chainable<void>;
      shouldShowSuccess(message?: string): Chainable<void>;
      shouldShowError(message?: string): Chainable<void>;
      deleteItem(itemName: string): Chainable<void>;
      editItem(itemName: string): Chainable<void>;
      searchInTable(searchTerm: string): Chainable<void>;
    }
  }
}

export {};
