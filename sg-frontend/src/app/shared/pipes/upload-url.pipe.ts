import { Pipe, PipeTransform } from '@angular/core';

/**
 * Pipe para converter URLs de uploads relativas em URLs absolutas do backend.
 * 
 * Problema: O Angular dev server não redireciona /uploads/ para o backend,
 * então precisamos usar a URL absoluta do backend.
 * 
 * Uso: <img [src]="foto | uploadUrl">
 * 
 * Se a URL já começa com http, retorna direto.
 * Se começa com /uploads/, converte para http://localhost:8080/api/uploads/
 */
@Pipe({ name: 'uploadUrl' })
export class UploadUrlPipe implements PipeTransform {

  // URL base do backend - mudar em produção
  private static readonly BACKEND_URL = 'http://localhost:8080';

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

    return UploadUrlPipe.BACKEND_URL + path;
  }
}
