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
    
    setupNodeEvents(on, config) {
      // Plugin de cobertura (requer @cypress/codecoverage)
      // on('before:browser:launch', (browser, launchOptions) => {
      //   // Instrumentação de código para cobertura
      // });
      
      return config;
    }
  }
});
