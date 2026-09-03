// ***********************************************************
// Arquivo de suporte do Cypress - carregado antes de cada arquivo de teste
// ***********************************************************

import './commands';

// Ignorar erros não capturados
Cypress.on('uncaught:exception', (err, runnable) => {
  // Retorna false para prevenir que o Cypress falhe o teste
  return false;
});

// Configuração global
beforeEach(() => {
  // Limpa cookies e localStorage antes de cada teste
  cy.clearCookies();
  cy.clearLocalStorage();
});
