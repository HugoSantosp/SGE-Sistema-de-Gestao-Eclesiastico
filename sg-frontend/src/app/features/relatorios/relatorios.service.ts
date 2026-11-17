import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RelatoriosService {
  constructor(private http: HttpClient) {}

  /**
   * Baixa um relatório do backend como Blob.
   * responseType blob é obrigatório: sem isso o Angular tentaria parsear o PDF/Excel como JSON.
   */
  baixar(endpoint: string): Observable<Blob> {
    return this.http.get(endpoint, { responseType: 'blob' });
  }
}
