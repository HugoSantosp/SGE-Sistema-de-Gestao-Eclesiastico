import { Pipe, PipeTransform } from '@angular/core';
import { getBackendUrl } from '../../core/config/backend-config';

/**
 * Pipe para converter URLs de uploads relativas em URLs absolutas.
 *
 * Em produção o frontend e backend estão em domínios diferentes, então as URLs
 * de imagem precisam ser absolutas (ex: https://backend.onrender.com/api/uploads/...).
 *
 * Uso: <img [src]="foto | uploadUrl">
 *
 * Se a URL já começa com http, retorna direto.
 * Caso contrário, prefixa com a URL do backend (via variável de ambiente).
 */
@Pipe({ name: 'uploadUrl' })
export class UploadUrlPipe implements PipeTransform {

  transform(fotoUrl: string | null | undefined): string {
    if (!fotoUrl) return '';

    // Se já é uma URL absoluta, retorna direto
    if (fotoUrl.startsWith('http')) return fotoUrl;

    // Normaliza o path
    let path = fotoUrl.startsWith('/') ? fotoUrl : '/' + fotoUrl;

    // Converte /uploads/ para /api/uploads/
    if (path.startsWith('/uploads/') && !path.startsWith('/api/uploads/')) {
      path = '/api' + path;
    }

    // Prefixa com a URL do backend se disponível
    const backendUrl = getBackendUrl();
    if (backendUrl) {
      return backendUrl + path;
    }

    // Fallback: retorna o path relativo (para dev com proxy)
    return path;
  }
}
