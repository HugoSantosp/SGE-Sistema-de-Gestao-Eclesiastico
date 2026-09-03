import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface EscalaResponse {
  id: number;
  titulo: string;
  publicToken: string;
  resultadoToken: string | null;
  aberta: boolean;
  datasCount: number;
  confirmacoesCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface EscalaDetalhada {
  id: number;
  titulo: string;
  publicToken: string;
  resultadoToken: string | null;
  aberta: boolean;
  datas: DataDetalhada[];
  confirmacoes: ConfirmacaoResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface DataDetalhada {
  id: number;
  nomeEvento: string;
  data: string;
  horario: string;
  local: string;
  designacoes: DesignacaoResponse[];
  musicas: MusicaDTO[];
}

export interface ConfirmacaoResponse {
  id: number;
  membroId: number | null;
  membroNome: string | null;
  nome: string;
  email: string | null;
  celular: string | null;
  dataIds: number[];
  createdAt: string;
}

export interface DesignacaoResponse {
  id: number;
  confirmacaoId: number;
  nomeIntegrante: string;
  instrumento: string;
  ordem: number;
}

export interface MusicaDTO {
  id?: number;
  nome: string;
  artista?: string;
  link?: string;
  ordem?: number;
}

export interface DesignacaoRequest {
  confirmacaoId: number;
  instrumento: string;
  ordem?: number;
}

@Injectable({ providedIn: 'root' })
export class EscalaService {
  private baseUrl = '/api/escalas';
  private publicoUrl = '/api/publico/escala';

  constructor(private http: HttpClient) {}

  // ===== Escalas (Admin) =====
  listar(): Observable<EscalaResponse[]> {
    return this.http.get<EscalaResponse[]>(this.baseUrl);
  }

  buscarPorId(id: number): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`${this.baseUrl}/${id}`);
  }

  criar(titulo: string): Observable<EscalaResponse> {
    return this.http.post<EscalaResponse>(this.baseUrl, { titulo });
  }

  toggleEscala(id: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/${id}/toggle`, {});
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  gerarLinkResultado(id: number): Observable<{ resultadoToken: string }> {
    return this.http.post<{ resultadoToken: string }>(`${this.baseUrl}/${id}/gerar-link`, {});
  }

  // ===== Datas =====
  adicionarData(escalaId: number, data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/${escalaId}/datas`, data);
  }

  removerData(escalaId: number, dataId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${escalaId}/datas/${dataId}`);
  }

  // ===== Designações =====
  salvarDesignacoes(escalaId: number, dataId: number, designacoes: DesignacaoRequest[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${escalaId}/datas/${dataId}/designacoes`, designacoes);
  }

  // ===== Músicas =====
  salvarMusicas(escalaId: number, dataId: number, musicas: MusicaDTO[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${escalaId}/datas/${dataId}/musicas`, musicas);
  }

  // ===== Instrumentos =====
  listarInstrumentos(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/instrumentos`);
  }

  // ===== PÚBLICO (sem autenticação) =====

  /** Busca escala pública por token (apenas datas, sem designações) */
  buscarEscalaPublica(token: string): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`${this.publicoUrl}/${token}`);
  }

  /** Confirma disponibilidade em escala pública */
  confirmarDisponibilidade(token: string, dados: {
    nome: string; email?: string; celular?: string; dataIds: number[]
  }): Observable<ConfirmacaoResponse> {
    return this.http.post<ConfirmacaoResponse>(`${this.publicoUrl}/${token}/confirmar`, dados);
  }

  /** Busca resultado da escala montada */
  buscarResultado(token: string): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`${this.publicoUrl}/resultado/${token}`);
  }

  /** Busca repertório do ministro (mesmo endpoint do resultado) */
  buscarRepertorioMinistro(token: string): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`${this.publicoUrl}/resultado/${token}/ministro`);
  }

  /** Salva músicas do repertório usando token público (ministro, sem JWT) */
  salvarMusicasPublico(token: string, dataId: number, musicas: MusicaDTO[]): Observable<void> {
    return this.http.post<void>(`${this.publicoUrl}/resultado/${token}/ministro/${dataId}`, musicas);
  }
}
