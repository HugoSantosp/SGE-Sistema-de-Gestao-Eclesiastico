import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Bispo } from '../../core/models/sge.models';

@Injectable({ providedIn: 'root' })
export class BisposService {
  private apiUrl = '/api/bispos';
  constructor(private http: HttpClient) {}
  listar(): Observable<Bispo[]> { return this.http.get<Bispo[]>(this.apiUrl); }
  buscarPorId(id: number): Observable<Bispo> { return this.http.get<Bispo>(`${this.apiUrl}/${id}`); }
  criar(data: any): Observable<Bispo> { return this.http.post<Bispo>(this.apiUrl, data); }
  atualizar(id: number, data: any): Observable<Bispo> { return this.http.put<Bispo>(`${this.apiUrl}/${id}`, data); }
  deletar(id: number): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}
