import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Membro } from '../../core/models/sge.models';

@Injectable({ providedIn: 'root' })
export class MembrosService {
  private apiUrl = '/api/membros';

  constructor(private http: HttpClient) {}

  listar(situacao?: 'ATIVO' | 'INATIVO'): Observable<Membro[]> {
    const url = situacao ? `${this.apiUrl}?situacao=${situacao}` : this.apiUrl;
    return this.http.get<Membro[]>(url);
  }

  buscarPorId(id: number): Observable<Membro> { return this.http.get<Membro>(`${this.apiUrl}/${id}`); }

  criar(data: any): Observable<Membro> { return this.http.post<Membro>(this.apiUrl, data); }

  atualizar(id: number, data: any): Observable<Membro> { return this.http.put<Membro>(`${this.apiUrl}/${id}`, data); }

  deletar(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}
