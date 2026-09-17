/**
 * Configuração central da URL do backend.
 *
 * Em tempo de execução, a URL é lida de window.__BACKEND_URL__, que é injetada
 * pelo pipeline de deploy (render, nginx, etc.) via tag <script> na página HTML.
 *
 * Fallback: environment.apiUrl (definido em tempo de build).
 *
 * Para configurar no Render (ou outro host):
 *   1. No painel do serviço frontend, adicione a variável de ambiente BACKEND_URL
 *      com o valor https://sge-sistema-de-gestao-eclesiastico.onrender.com
 *   2. O script de injeção <script>window.__BACKEND_URL__='...';</script> será
 *      gerado automaticamente pelo CI antes do build, ou pode ser adicionado
 *      manualmente no index.html se necessário.
 *
 * Uso: importe { getBackendUrl } de 'src/app/core/config/backend-config'.
 */
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    __BACKEND_URL__?: string;
  }
}

/**
 * Retorna a URL do backend para usar nas chamadas HTTP.
 *
 * Prioridade:
 *   1. window.__BACKEND_URL__ (injetada em tempo de execução pelo deploy)
 *   2. environment.apiUrl (definida em tempo de build)
 */
export function getBackendUrl(): string {
  if (typeof window !== 'undefined' && window.__BACKEND_URL__) {
    return window.__BACKEND_URL__;
  }
  return environment.apiUrl || '';
}

/**
 * Monta uma URL completa de API: backendUrl + path.
 * Exemplo: getApiUrl('/auth/login') → 'https://...onrender.com/auth/login'
 */
export function getApiUrl(path: string): string {
  const base = getBackendUrl();
  if (!base) return path;
  const cleanPath = path.startsWith('/') ? path : '/' + path;
  return base + cleanPath;
}
