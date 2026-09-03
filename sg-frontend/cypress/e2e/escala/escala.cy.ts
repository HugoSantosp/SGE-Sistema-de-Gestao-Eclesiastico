/// <reference types="cypress" />

describe('Escala de Louvor - Testes E2E', () => {
  beforeEach(() => {
    // Faz login
    cy.loginViaApi(
      Cypress.env('adminEmail'),
      Cypress.env('adminPassword')
    );
    cy.visit('/escala');
  });

  describe('Listagem de Escalas', () => {
    it('deve exibir página de escalas', () => {
      cy.url().should('include', '/escala');
      cy.contains('Escala').should('be.visible');
      cy.get('table, .table, [data-cy="escala-table"]').should('be.visible');
    });

    it('deve exibir botão de nova escala', () => {
      cy.get('a[href*="novo"], button:contains("Nova"), [data-cy="btn-nova-escala"]')
        .should('be.visible');
    });

    it('deve exibir status da escala (aberta/fechada)', () => {
      cy.get('.badge, .status, [data-cy="status-escala"]').should('exist');
    });
  });

  describe('Criação de Escala', () => {
    it('deve criar nova escala', () => {
      // Clica em nova escala
      cy.get('a[href*="novo"], button:contains("Nova"), [data-cy="btn-nova-escala"]')
        .click();
      
      // Preenche título
      cy.get('#titulo, [name="titulo"]')
        .type('Escala Domingo - Teste E2E');
      
      // Salva
      cy.submitForm();
      
      // Verifica sucesso
      cy.shouldShowSuccess();
      cy.url().should('include', '/escala');
    });
  });

  describe('Montagem de Escala', () => {
    it('deve acessar montagem da escala', () => {
      // Clica em uma escala existente
      cy.get('table tbody tr').first()
        .find('button[title="Montar"], a[title="Montar"], .btn-montar')
        .click();
      
      // Verifica se a página de montagem carregou
      cy.url().should('include', 'montar');
      cy.contains('Montar Escala').should('be.visible');
    });

    it('deve adicionar data à escala', () => {
      // Acessa montagem
      cy.get('table tbody tr').first()
        .find('button[title="Montar"], a[title="Montar"], .btn-montar')
        .click();
      
      // Clica em adicionar data
      cy.get('button:contains("Adicionar Data"), [data-cy="btn-adicionar-data"]')
        .click();
      
      // Preenche formulário de data
      cy.get('#dataEscala, [name="dataEscala"]')
        .type('2026-12-25');
      cy.get('#horario, [name="horario"]')
        .type('19:00');
      cy.get('#local, [name="local"]')
        .type('Templo Principal');
      
      // Salva
      cy.get('button:contains("Salvar")').last().click();
      
      // Verifica que a data foi adicionada
      cy.contains('25/12/2026').should('be.visible');
    });
  });

  describe('Designação de Membros', () => {
    it('deve designar membro para função', () => {
      // Acessa montagem
      cy.get('table tbody tr').first()
        .find('button[title="Montar"], a[title="Montar"], .btn-montar')
        .click();
      
      // Seleciona uma data
      cy.get('.data-card, [data-cy="data-card"]').first().click();
      
      // Clica em designar
      cy.get('button:contains("Designar"), [data-cy="btn-designar"]')
        .click();
      
      // Seleciona membro
      cy.get('#membroId, [name="membroId"]').select(1);
      
      // Seleciona função
      cy.get('#funcao, [name="funcao"]').select('Vocalista');
      
      // Salva
      cy.get('button:contains("Salvar")').last().click();
      
      // Verifica que a designação aparece
      cy.contains('Vocalista').should('be.visible');
    });
  });

  describe('Status da Escala', () => {
    it('deve abrir/fechar escala', () => {
      // Encontra uma escala fechada
      cy.get('.badge-secondary, [data-cy="status-fechada"]').first()
        .parent('tr')
        .find('button[title="Abrir"], .btn-abrir')
        .click();
      
      // Confirma ação
      cy.get('.swal2-confirm, button:contains("Sim")').click();
      
      // Verifica que o status mudou
      cy.get('.badge-success, [data-cy="status-aberta"]').should('exist');
    });
  });
});

describe('Escala - Testes de Fluxo Completo', () => {
  it('fluxo completo: criar → montar → designar → abrir', () => {
    cy.loginViaApi(
      Cypress.env('adminEmail'),
      Cypress.env('adminPassword')
    );
    
    // 1. Criar escala
    cy.visit('/escala/novo');
    cy.get('#titulo, [name="titulo"]').type('Escala Fluxo Completo');
    cy.submitForm();
    cy.shouldShowSuccess();
    
    // 2. Montar escala
    cy.contains('Escala Fluxo Completo')
      .parent('tr')
      .find('button[title="Montar"], .btn-montar')
      .click();
    
    // 3. Adicionar data
    cy.get('button:contains("Adicionar Data"), [data-cy="btn-adicionar-data"]')
      .click();
    cy.get('#dataEscala, [name="dataEscala"]').type('2026-12-25');
    cy.get('#horario, [name="horario"]').type('19:00');
    cy.get('button:contains("Salvar")').last().click();
    
    // 4. Designar membro
    cy.get('.data-card, [data-cy="data-card"]').first().click();
    cy.get('button:contains("Designar"), [data-cy="btn-designar"]').click();
    cy.get('#membroId, [name="membroId"]').select(1);
    cy.get('#funcao, [name="funcao"]').select('Vocalista');
    cy.get('button:contains("Salvar")').last().click();
    
    // 5. Abrir escala
    cy.visit('/escala');
    cy.contains('Escala Fluxo Completo')
      .parent('tr')
      .find('button[title="Abrir"], .btn-abrir')
      .click();
    cy.get('.swal2-confirm, button:contains("Sim")').click();
    
    // 6. Verificar que está aberta
    cy.contains('Escala Fluxo Completo')
      .parent('tr')
      .find('.badge-success, [data-cy="status-aberta"]')
      .should('be.visible');
  });
});
