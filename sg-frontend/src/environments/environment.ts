export const environment = {
  production: false,
  // URL do backend — em produção essa URL é injetada em tempo de execução via
  // window.__BACKEND_URL__ (veja src/app/core/config/backend-config.ts).
  // Fallback usado quando a variável não está definida (dev local sem injeção).
  apiUrl: 'http://localhost:8080'
};
