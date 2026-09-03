// ===== Auth =====

export interface LoginRequest {
  user: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  nome: string;
  nivel: string;
  id: number;
  senhaTemporaria: boolean;
}

export interface ApiMessageResponse {
  message: string;
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
}

// ===== MeuMinisterio =====

/** Papéis que um membro pode ter dentro de um ministério (papéis compostos). */
export type PapelMinisterio =
  | 'LIDER'
  | 'INTEGRANTE'
  | 'MUSICO'
  | 'VOCALISTA'
  | 'BACKING_VOCAL'
  | 'TECNICO'
  | 'OPERADOR'
  | 'INTERCESSOR'
  | 'RECEPCAO'
  | 'FACILITADOR';

/** Rótulo amigável de cada papel (exibido em badges e selects). */
export const PAPEL_MINISTERIO_LABELS: Record<PapelMinisterio, string> = {
  LIDER: 'Líder',
  INTEGRANTE: 'Integrante',
  MUSICO: 'Músico',
  VOCALISTA: 'Vocalista',
  BACKING_VOCAL: 'Backing vocal',
  TECNICO: 'Técnico',
  OPERADOR: 'Operador',
  INTERCESSOR: 'Intercessor',
  RECEPCAO: 'Recepção',
  FACILITADOR: 'Facilitador'
};

/** Lista ordenada de papéis para preencher selects (Líder e Integrante primeiro). */
export const PAPEL_MINISTERIO_LIST: PapelMinisterio[] = [
  'LIDER', 'INTEGRANTE', 'MUSICO', 'VOCALISTA', 'BACKING_VOCAL',
  'TECNICO', 'OPERADOR', 'INTERCESSOR', 'RECEPCAO', 'FACILITADOR'
];

export function papelMinisterioLabel(papel: PapelMinisterio | null | undefined): string {
  return papel ? PAPEL_MINISTERIO_LABELS[papel] : '';
}

export interface MinisterioDoUsuario {
  id: number;
  nome: string;
  descricao: string | null;
  foto: string | null;
  papel: PapelMinisterio | null;
}

export interface MembroMinisterio {
  vinculoId: number;
  membroId: number;
  nome: string;
  documento: string | null;
  telefone: string | null;
  foto: string | null;
  papel: PapelMinisterio;
}

export interface EscalaResponse {
  id: number;
  titulo: string;
  ministerioId: number | null;
  publicToken: string;
  resultadoToken: string | null;
  aberta: boolean;
  datasCount: number;
  confirmacoesCount: number;
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

export interface EscalaDetalhada {
  id: number;
  titulo: string;
  ministerioId: number | null;
  publicToken: string;
  resultadoToken: string | null;
  aberta: boolean;
  datas: DataDetalhada[];
  confirmacoes: ConfirmacaoResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface ConfirmarEscalaRequest {
  dataIds: number[];
}

export interface AdicionarMembroRequest {
  membroId: number;
  papel: PapelMinisterio;
}

export interface EscalaDataDTO {
  id?: number;
  nomeEvento: string;
  data: string;      // yyyy-MM-dd
  horario: string;   // HH:mm:ss
  local: string;
}

export interface DesignacaoRequest {
  confirmacaoId: number;
  instrumento: string;
  ordem?: number;
}

export interface MembroSimple {
  id: number;
  nome: string;
  documento: string | null;
  telefone: string | null;
}

export interface MusicaSimple {
  id?: number;
  nome: string;
  artista?: string;
  link?: string;
  ordem?: number;
}
