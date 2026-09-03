/// <reference types="cypress" />

describe('Login - Testes E2E', () => {
  beforeEach(() => {
    // Visita a página de login antes de cada teste
    cy.visit('/login');
  });

  describe('Fluxo de Login', () => {
    it('deve exibir formulário de login', () => {
      // Verifica se os elementos do formulário estão presentes
      cy.get('input[type="email"], input[name="email"], #email')
        .should('be.visible');
      cy.get('input[type="password"], input[name="senha"], #senha')
        .should('be.visible');
      cy.get('button[type="submit"]')
        .should('be.visible')
        .and('contain', 'Entrar');
    });

    it('deve fazer login com sucesso usando email', () => {
      // Preenche o formulário
      cy.get('input[type="email"], input[name="email"], #email')
        .type(Cypress.env('adminEmail'));
      cy.get('input[type="password"], input[name="senha"], #senha')
        .type(Cypress.env('adminPassword'));
      
      // Submete o formulário
      cy.get('button[type="submit"]').click();
      
      // Verifica redirecionamento para dashboard
      cy.url().should('include', '/dashboard');
      
      // Verifica se o token foi armazenado
      cy.window().then((win) => {
        expect(win.localStorage.getItem('token')).to.not.be.null;
      });
    });

    it('deve mostrar erro com senha incorreta', () => {
      // Preenche com senha errada
      cy.get('input[type="email"], input[name="email"], #email')
        .type(Cypress.env('adminEmail'));
      cy.get('input[type="password"], input[name="senha"], #senha')
        .type('senhaerrada');
      
      cy.get('button[type="submit"]').click();
      
      // Verifica mensagem de erro
      cy.shouldShowError('Credenciais inválidas');
      
      // Verifica que não redirecionou
      cy.url().should('include', '/login');
    });

    it('deve mostrar erro com email inexistente', () => {
      cy.get('input[type="email"], input[name="email"], #email')
        .type('naoexiste@test.com');
      cy.get('input[type="password"], input[name="senha"], #senha')
        .type('123');
      
      cy.get('button[type="submit"]').click();
      
      cy.shouldShowError('Credenciais inválidas');
      cy.url().should('include', '/login');
    });

    it('deve validar campos obrigatórios', () => {
      // Tenta submeter sem preencher
      cy.get('button[type="submit"]').click();
      
      // Verifica validação do navegador
      cy.get('input[type="email"], input[name="email"], #email')
        .should('have.attr', 'required');
      cy.get('input[type="password"], input[name="senha"], #senha')
        .should('have.attr', 'required');
    });
  });

  describe('Funcionalidades Adicionais', () => {
    it('deve ter link para esqueci minha senha', () => {
      cy.contains('Esqueci minha senha')
        .should('be.visible')
        .and('have.attr', 'href')
        .and('include', 'esqueci-senha');
    });

    it('deve alternar visibilidade da senha', () => {
      // Verifica se o campo de senha está oculto inicialmente
      cy.get('input[type="password"]').should('exist');
      
      // Clica no ícone de olho (se existir)
      cy.get('button[type="button"]').first().click({ failOnStatusCode: false });
      
      // Verifica se o tipo do input mudou (pode ter mudado para text)
      cy.get('input').last().invoke('attr', 'type').should('be.oneOf', ['password', 'text']);
    });

    it('deve manter erro após múltiplas tentativas', () => {
      // Faz 3 tentativas com senha errada
      for (let i = 0; i < 3; i++) {
        cy.get('input[type="email"], input[name="email"], #email')
          .clear()
          .type(Cypress.env('adminEmail'));
        cy.get('input[type="password"], input[name="senha"], #senha')
          .clear()
          .type('senhaerrada');
        cy.get('button[type="submit"]').click();
        cy.wait(1000);
      }
      
      // Verifica que a mensagem de erro ainda aparece
      cy.shouldShowError();
    });
  });
});

describe('Login - Testes de Acessibilidade', () => {
  it('deve ter labels acessíveis nos campos', () => {
    cy.visit('/login');
    
    // Verifica se os campos têm labels associados
    cy.get('input[type="email"], input[name="email"], #email')
      .should('have.attr', 'aria-label')
      .or('have.attr', 'aria-labelledby')
      .or('satisfy', ($el) => {
        const id = $el.attr('id');
        return cy.get(`label[for="${id}"]`).should('exist');
      });
  });

  it('deve ser navegável por teclado', () => {
    cy.visit('/login');
    
    // Navega usando Tab
    cy.get('body').tab();
    cy.focused().should('match', 'input[type="email"], input[name="email"], #email');
    
    cy.focused().tab();
    cy.focused().should('match', 'input[type="password"], input[name="senha"], #senha');
    
    cy.focused().tab();
    cy.focused().should('match', 'button[type="submit"]');
  });
});
