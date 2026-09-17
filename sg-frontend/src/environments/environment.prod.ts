export const environment = {
  production: true,
  // URL do backend — em produção essa URL é injetada em tempo de execução via
  // window.__BACKEND_URL__ (veja src/app/core/config/backend-config.ts).
  // Fallback usado quando a variável não está definida.
  apiUrl: 'https://sge-sistema-de-gestao-eclesiastico.onrender.com'
};
