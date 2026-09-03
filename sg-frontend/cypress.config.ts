import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    specPattern: 'cypress/e2e/**/*.cy.ts',
    supportFile: 'cypress/support/e2e.ts',
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 30000,
    
    // Configuração de retry para testes instáveis
    retries: {
      runMode: 2,
      openMode: 0
    },

    // Variáveis de ambiente para testes
    env: {
      apiUrl: 'http://localhost:8080',
      adminEmail: 'admin@icert.local',
      adminPassword: '123'
    },

    setupNodeEvents(on, config) {
      // Implementar listeners de eventos aqui se necessário
      on('task', {
        // Exemplo de task personalizado
        log(message) {
          console.log(message);
          return null;
        }
      });
    }
  }
});
