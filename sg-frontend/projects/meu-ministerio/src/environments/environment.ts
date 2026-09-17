export const environment = {
  production: false,
  // URL do backend — em produção injetada via window.__BACKEND_URL__
  // (build de produção usa environment.prod.ts via fileReplacements no angular.json)
  apiUrl: 'http://localhost:8080'
};
