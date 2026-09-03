/// <reference types="cypress" />

// Plugin do Cypress para configurações adicionais
// Este arquivo é carregado antes de cada arquivo de teste

/**
 * Plugin principal do Cypress
 */
module.exports = (on, config) => {
  // Configurações de tarefas customizadas
  on('task', {
    // Log no console do Node
    log(message) {
      console.log(message);
      return null;
    },

    // Debugger (para debug em testes)
    debug(message) {
      console.debug(message);
      return null;
    },

    // Verificar se existe arquivo
    fileExists(filePath) {
      const fs = require('fs');
      return fs.existsSync(filePath);
    },

    // Ler arquivo
    readFile(filePath) {
      const fs = require('fs');
      return fs.readFileSync(filePath, 'utf8');
    },

    // Escrever arquivo
    writeFile({ filePath, content }) {
      const fs = require('fs');
      fs.writeFileSync(filePath, content);
      return null;
    }
  });

  // Configurações globais
  config.defaultCommandTimeout = 10000;
  config.requestTimeout = 10000;
  config.responseTimeout = 30000;

  return config;
};
