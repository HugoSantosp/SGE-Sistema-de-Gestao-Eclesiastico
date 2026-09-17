/**
 * Configuração central da URL do backend (app MeuMinisterio).
 *
 * Em tempo de execução, a URL é lida de window.__BACKEND_URL__, que é injetada
 * pelo pipeline de deploy via tag <script> na página HTML.
 *
 * Fallback: environment.apiUrl (definido em tempo de build).
 */
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    __BACKEND_URL__?: string;
  }
}

export function getBackendUrl(): string {
  if (typeof window !== 'undefined' && window.__BACKEND_URL__) {
    return window.__BACKEND_URL__;
  }
  return environment.apiUrl || '';
}

export function getApiUrl(path: string): string {
  const base = getBackendUrl();
  if (!base) return path;
  const cleanPath = path.startsWith('/') ? path : '/' + path;
  return base + cleanPath;
}
