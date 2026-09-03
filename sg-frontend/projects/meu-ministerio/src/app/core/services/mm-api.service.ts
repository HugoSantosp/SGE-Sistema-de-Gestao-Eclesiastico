import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  MinisterioDoUsuario, EscalaResponse, EscalaDetalhada,
  ConfirmarEscalaRequest, ConfirmacaoResponse, MembroMinisterio,
  EscalaDataDTO, DesignacaoRequest, MembroSimple, MusicaSimple, MusicaDTO
} from '../models/mm.models';

@Injectable({ providedIn: 'root' })
export class MmApiService {
  private baseUrl = '/api/meu-ministerio';

  constructor(private http: HttpClient) {}

  // ===== Ministérios =====

  meusMinisterios(): Observable<MinisterioDoUsuario[]> {
    return this.http.get<MinisterioDoUsuario[]>(`${this.baseUrl}/ministerios`);
  }

  ministeriosLiderados(): Observable<MinisterioDoUsuario[]> {
    return this.http.get<MinisterioDoUsuario[]>(`${this.baseUrl}/ministerios/liderados`);
  }

  membrosDoMinisterio(ministerioId: number): Observable<MembroMinisterio[]> {
    return this.http.get<MembroMinisterio[]>(`${this.baseUrl}/ministerios/${ministerioId}/membros`);
  }

  adicionarMembro(ministerioId: number, dto: { membroId: number; papel: string }): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/ministerios/${ministerioId}/membros`, dto);
  }

  removerMembro(ministerioId: number, membroId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/ministerios/${ministerioId}/membros/${membroId}`);
  }

  alterarPapel(ministerioId: number, membroId: number, papel: string): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/ministerios/${ministerioId}/membros/${membroId}`, { papel });
  }

  // ===== Escalas =====

  minhasEscalas(): Observable<EscalaResponse[]> {
    return this.http.get<EscalaResponse[]>(`${this.baseUrl}/escalas`);
  }

  escalasDoMinisterio(ministerioId: number): Observable<EscalaResponse[]> {
    return this.http.get<EscalaResponse[]>(`${this.baseUrl}/ministerios/${ministerioId}/escalas`);
  }

  buscarEscala(escalaId: number): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`${this.baseUrl}/escalas/${escalaId}`);
  }

  // ===== Líder: membros disponíveis (todos os membros da igreja) =====

  listarMembros(): Observable<MembroSimple[]> {
    return this.http.get<MembroSimple[]>('/api/membros');
  }

  // ===== Líder: escalas do ministério =====

  criarEscala(ministerioId: number, titulo: string): Observable<EscalaResponse> {
    return this.http.post<EscalaResponse>(`${this.baseUrl}/ministerios/${ministerioId}/escalas`, { titulo });
  }

  adicionarData(escalaId: number, data: EscalaDataDTO): Observable<EscalaDataDTO> {
    return this.http.post<EscalaDataDTO>(`${this.baseUrl}/escalas/${escalaId}/datas`, data);
  }

  removerData(escalaId: number, dataId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/escalas/${escalaId}/datas/${dataId}`);
  }

  salvarDesignacoes(escalaId: number, dataId: number, designacoes: DesignacaoRequest[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/escalas/${escalaId}/datas/${dataId}/designacoes`, designacoes);
  }

  salvarMusicas(escalaId: number, dataId: number, musicas: MusicaSimple[]): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/escalas/${escalaId}/datas/${dataId}/musicas`, musicas);
  }

  listarInstrumentos(): Observable<string[]> {
    return this.http.get<string[]>('/api/escalas/instrumentos');
  }

  toggleEscala(escalaId: number): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/escalas/${escalaId}/toggle`, {});
  }

  gerarLinkResultado(escalaId: number): Observable<{ resultadoToken: string }> {
    return this.http.post<{ resultadoToken: string }>(`${this.baseUrl}/escalas/${escalaId}/gerar-link`, {});
  }

  // ===== Confirmação de disponibilidade =====

  confirmarDisponibilidade(escalaId: number, dataIds: number[]): Observable<ConfirmacaoResponse> {
    const body: ConfirmarEscalaRequest = { dataIds };
    return this.http.post<ConfirmacaoResponse>(`${this.baseUrl}/escalas/${escalaId}/confirmar`, body);
  }

  cancelarConfirmacao(escalaId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/escalas/${escalaId}/confirmar`);
  }

  // ===== PÚBLICO (links com token — sem login) =====

  /** Busca escala pública por token (apenas datas) */
  buscarEscalaPublica(token: string): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`/api/publico/escala/${token}`);
  }

  /** Confirma disponibilidade em escala pública via token */
  confirmarDisponibilidadePublica(
    token: string,
    dados: { nome: string; email?: string; celular?: string; dataIds: number[] }
  ): Observable<ConfirmacaoResponse> {
    return this.http.post<ConfirmacaoResponse>(`/api/publico/escala/${token}/confirmar`, dados);
  }

  /** Busca resultado da escala montada (token do resultado) */
  buscarResultadoPublico(token: string): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`/api/publico/escala/resultado/${token}`);
  }

  /** Busca repertório do ministro (token do resultado) */
  buscarRepertorioMinistro(token: string): Observable<EscalaDetalhada> {
    return this.http.get<EscalaDetalhada>(`/api/publico/escala/resultado/${token}/ministro`);
  }

  /** Salva músicas do repertório via token público (ministro, sem JWT) */
  salvarMusicasPublico(token: string, dataId: number, musicas: MusicaDTO[]): Observable<void> {
    return this.http.post<void>(`/api/publico/escala/resultado/${token}/ministro/${dataId}`, musicas);
  }
}
