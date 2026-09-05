import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe para converter URLs de uploads relativas em URLs servíveis.
 *
 * As fotos são servidas pelo backend em /api/uploads/** (mesma origem em
 * produção, via proxy do Nginx; em dev, o proxy do Angular CLI encaminha
 * /api para o backend). Por isso o pipe devolve caminhos RELATIVOS —
 * em produção o domínio é o mesmo, não há host fixo.
 *
 * Uso: <img [src]="foto | uploadUrl">
 *
 * Se a URL já começa com http, retorna direto.
 * Se começa com /uploads/, converte para /api/uploads/
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

    return path;
  }
}
