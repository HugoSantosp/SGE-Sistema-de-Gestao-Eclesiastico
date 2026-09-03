import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe para converter URLs de uploads relativas em URLs absolutas do backend.
 */
@Pipe({ name: 'uploadUrl' })
export class UploadUrlPipe implements PipeTransform {

  private static readonly BACKEND_URL = 'http://localhost:8080';

  transform(fotoUrl: string | null | undefined): string {
    if (!fotoUrl) return '';

    if (fotoUrl.startsWith('http')) return fotoUrl;

    let path = fotoUrl.startsWith('/') ? fotoUrl : '/' + fotoUrl;

    if (path.startsWith('/uploads/') && !path.startsWith('/api/uploads/')) {
      path = '/api' + path;
    }

    return UploadUrlPipe.BACKEND_URL + path;
  }
}
