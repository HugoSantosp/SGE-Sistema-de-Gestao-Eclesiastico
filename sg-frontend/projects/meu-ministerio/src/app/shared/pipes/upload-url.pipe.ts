import { Pipe, PipeTransform } from '@angular/core';
import { getBackendUrl } from '../../core/config/backend-config';

/**
 * Pipe para converter URLs de uploads relativas em URLs absolutas (app MeuMinisterio).
 *
 * Uso: <img [src]="foto | uploadUrl">
 */
@Pipe({ name: 'uploadUrl' })
export class UploadUrlPipe implements PipeTransform {

  transform(fotoUrl: string | null | undefined): string {
    if (!fotoUrl) return '';

    if (fotoUrl.startsWith('http')) return fotoUrl;

    let path = fotoUrl.startsWith('/') ? fotoUrl : '/' + fotoUrl;

    if (path.startsWith('/uploads/') && !path.startsWith('/api/uploads/')) {
      path = '/api' + path;
    }

    const backendUrl = getBackendUrl();
    if (backendUrl) {
      return backendUrl + path;
    }

    return path;
  }
}
