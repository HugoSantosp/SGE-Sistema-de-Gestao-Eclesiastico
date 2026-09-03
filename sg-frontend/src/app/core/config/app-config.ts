/**
 * Configurações do app ADMIN (SGE-Administrativo).
 *
 * As páginas PÚBLICAS da Escala de Louvor (confirmação, resultado e
 * repertório do ministro) são hospedadas pelo app MeuMinistério
 * (icertag.com.br/SGE-MeuMinisterio). Os links gerados pelo admin
 * apontam para lá.
 */
export const APP_CONFIG = {
  /** Base do app MeuMinistério em produção */
  ministerioBase: '/SGE-MeuMinisterio',
  /** URL de dev do app MeuMinistério (ng serve do projeto meu-ministerio) */
  ministerioDevUrl: 'http://localhost:4300'
};

/** URL completa de um caminho público hospedado pelo app MeuMinistério */
export function ministerioUrl(path: string): string {
  const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  const base = isLocal ? APP_CONFIG.ministerioDevUrl : `${window.location.origin}${APP_CONFIG.ministerioBase}`;
  return `${base}${path}`;
}
