import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe para converter URLs de uploads relativas em URLs servíveis.
 *
 * As fotos são servidas pelo backend em /api/uploads/** (mesma origem em
 * produção, via proxy do Nginx; em dev, o proxy do Angular CLI encaminha
 * /api para o backend). Por isso o pipe devolve caminhos RELATIVOS —
 * em produção o domínio é o mesmo, não há host fixo.
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

    return path;
  }
}
