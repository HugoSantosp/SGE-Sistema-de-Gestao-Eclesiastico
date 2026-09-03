/// <reference types="cypress" />

describe('Membros - Testes E2E', () => {
  beforeEach(() => {
    // Faz login antes de cada teste
    cy.loginViaApi(
      Cypress.env('adminEmail'),
      Cypress.env('adminPassword')
    );
    cy.visit('/membros');
  });

  describe('Listagem de Membros', () => {
    it('deve exibir página de membros', () => {
      // Verifica se está na página correta
      cy.url().should('include', '/membros');
      
      // Verifica se o título da página está presente
      cy.contains('Membros').should('be.visible');
      
      // Verifica se a tabela de membros existe
      cy.get('table, .table, [data-cy="membros-table"]')
        .should('be.visible');
    });

    it('deve exibir botão de novo membro', () => {
      cy.get('a[href*="novo"], button:contains("Novo"), [data-cy="btn-novo-membro"]')
        .should('be.visible');
    });

    it('deve permitir buscar membros', () => {
      // Verifica se existe campo de busca
      cy.get('input[type="search"], input[placeholder*="Buscar"], input[placeholder*="buscar"]')
        .should('be.visible');
    });
  });

  describe('Cadastro de Membros', () => {
    it('deve abrir formulário de novo membro', () => {
      // Clica no botão de novo membro
      cy.get('a[href*="novo"], button:contains("Novo"), [data-cy="btn-novo-membro"]')
        .click();
      
      // Verifica se o formulário foi carregado
      cy.url().should('include', 'novo');
      cy.get('form').should('be.visible');
    });

    it('deve cadastrar novo membro com sucesso', () => {
      // Navega para o formulário
      cy.get('a[href*="novo"], button:contains("Novo"), [data-cy="btn-novo-membro"]')
        .click();
      
      // Preenche o formulário
      cy.fillForm({
        'nome': 'João Silva Teste',
        'documento': '12345678901',
        'telefone': '(11) 99999-9999',
        'email': 'joao.teste@email.com'
      });
      
      // Seleciona situação
      cy.get('#situacao, [name="situacao"]').select('ATIVO');
      
      // Submete o formulário
      cy.submitForm();
      
      // Verifica mensagem de sucesso
      cy.shouldShowSuccess();
      
      // Verifica redirecionamento para listagem
      cy.url().should('include', '/membros');
    });

    it('deve validar campos obrigatórios', () => {
      cy.get('a[href*="novo"], button:contains("Novo"), [data-cy="btn-novo-membro"]')
        .click();
      
      // Tenta submeter sem preencher
      cy.submitForm();
      
      // Verifica mensagens de erro de validação
      cy.get('.invalid-feedback, .text-danger, [data-cy="error"]')
        .should('be.visible');
    });
  });

  describe('Edição de Membros', () => {
    it('deve abrir formulário de edição', () => {
      // Clica em editar no primeiro registro
      cy.get('table tbody tr').first()
        .find('button[title="Editar"], a[title="Editar"], .btn-edit')
        .click();
      
      // Verifica se o formulário foi carregado com dados
      cy.get('form').should('be.visible');
      cy.get('input[name="nome"], #nome').should('not.have.value', '');
    });

    it('deve atualizar membro com sucesso', () => {
      // Busca um membro para editar
      cy.searchInTable('João');
      
      // Clica em editar
      cy.editItem('João Silva Teste');
      
      // Atualiza o telefone
      cy.get('#telefone, [name="telefone"]')
        .clear()
        .type('(11) 88888-8888');
      
      // Salva
      cy.submitForm();
      
      // Verifica sucesso
      cy.shouldShowSuccess();
    });
  });

  describe('Exclusão de Membros', () => {
    it('deve confirmar antes de deletar', () => {
      // Busca um membro para deletar
      cy.searchInTable('João');
      
      // Clica em deletar
      cy.get('table tbody tr').first()
        .find('button[title="Excluir"], .btn-delete')
        .click();
      
      // Verifica se o modal de confirmação apareceu
      cy.get('.swal2-popup, .modal, [data-cy="confirm-dialog"]')
        .should('be.visible');
      
      // Clica em cancelar
      cy.get('.swal2-cancel, button:contains("Cancelar"), [data-cy="btn-cancelar"]')
        .click();
      
      // Verifica que o registro ainda existe
      cy.contains('João Silva Teste').should('exist');
    });
  });

  describe('Paginação e Busca', () => {
    it('deve filtrar membros por busca', () => {
      // Digita no campo de busca
      cy.searchInTable('João');
      
      // Verifica se apenas os resultados relevantes aparecem
      cy.get('table tbody tr').each(($row) => {
        cy.wrap($row).should('contain', 'João');
      });
    });

    it('deve paginação funcionar', () => {
      // Verifica se existe paginação
      cy.get('.pagination, nav[aria-label="Paginação"]')
        .should('exist')
        .then(($pagination) => {
          if ($pagination.find('button, a').length > 1) {
            // Clica na próxima página
            cy.get('.pagination .next, .pagination button:last-child').click();
            cy.waitForLoad();
          }
        });
    });
  });
});

describe('Membros - Testes de Acessibilidade', () => {
  beforeEach(() => {
    cy.loginViaApi(
      Cypress.env('adminEmail'),
      Cypress.env('adminPassword')
    );
    cy.visit('/membros');
  });

  it('deve ter heading correto', () => {
    cy.get('h1, h2, h3').should('contain', 'Membros');
  });

  it('deve ter navegação por teclado', () => {
    cy.get('body').tab();
    cy.focused().should('have.attr', 'href').or('be.visible');
  });
});
