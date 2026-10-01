/**
 * Configurações do app MeuMinistério.
 *
 * `selfBase`/`selfDevUrl`: base do PRÓPRIO app MeuMinistério (que hospeda as
 * páginas públicas de escala: confirmação, resultado e repertório do ministro).
 * Em produção o app é servido na RAIZ do domínio (ex.: meuministerio.icertag.com.br),
 * em dev na porta 4300.
 *
 * `adminAppBase`/`adminDevUrl`: base do app ADMIN (painel), usado apenas para
 * referências cruzadas quando necessário.
 */
export const MM_CONFIG = {
  // App publicado na raiz do domínio próprio — sem prefixo de subpasta.
  selfBase: '',
  selfDevUrl: 'http://localhost:4300',

  adminAppBase: '/SGE-Administracao',
  adminDevUrl: 'http://localhost:4200'
};

/** URL base atual do app MeuMinistério (dev → porta 4300; prod → raiz do domínio atual) */
export function mmSelfUrl(path: string): string {
  const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
  const base = isLocal ? MM_CONFIG.selfDevUrl : `${window.location.origin}${MM_CONFIG.selfBase}`;
  return `${base}${path}`;
}
