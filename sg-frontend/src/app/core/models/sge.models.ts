export interface LoginRequest {
  user: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  tipo: string;
  nome: string;
  nivel: string;
  idUsuario: number;
  senhaTemporaria: boolean;
}

export interface Usuario {
  id: number;
  nome: string;
  documento: string;
  email: string;
  senha: string;
  nivel: NivelAcesso;
  idPessoa: number;
  foto: string;
}

export interface Bispo {
  id: number;
  nome: string;
  email: string;
  documento: string;
  telefone: string;
  endereco: string;
  foto: string;
  dataCad: string;
  dataNasc: string;
}

export interface Presbitero {
  id: number;
  nome: string;
  email: string;
  documento: string;
  telefone: string;
  endereco: string;
  foto: string;
  dataCad: string;
  dataNasc: string;
  obs: string;
}

export interface Tesoureiro {
  id: number;
  nome: string;
  email: string;
  documento: string;
  telefone: string;
  endereco: string;
  foto: string;
  dataCad: string;
  dataNasc: string;
}

export interface Secretario {
  id: number;
  nome: string;
  email: string;
  documento: string;
  telefone: string;
  endereco: string;
  foto: string;
  dataCad: string;
  dataNasc: string;
}

export interface Membro {
  id: number;
  nome: string;
  documento: string;
  telefone: string;
  endereco: string;
  foto: string;
  dataCad: string;
  dataNasc: string;
  situacao: StatusMembro;
  funcaoId: number;
  ministerioId: number;
  ministerioNome?: string;
  dataBatismo: string;
  obs: string;
}

export interface Cargo {
  id: number;
  nome: string;
}

export interface Fornecedor {
  id: number;
  nome: string;
  telefone: string;
  endereco: string;
  email: string;
  produto: string;
}

export interface ContaPagar {
  id: number;
  descricao: string;
  fornecedorId: number;
  valor: number;
  dataCad: string;
  vencimento: string;
  usuarioCadId: number;
  usuarioBaixaId: number;
  dataBaixa: string;
  frequencia: FrequenciaPagamento;
  status: StatusConta;
  arquivo: string;
}

export interface ContaReceber {
  id: number;
  descricao: string;
  valor: number;
  dataCad: string;
  vencimento: string;
  dataRecebimento: string;
  frequencia: FrequenciaPagamento;
  status: StatusConta;
  contribuinte: string;
}

export interface Tarefa {
  id: number;
  titulo: string;
  descricao: string;
  horaTarefa: string;
  dataTarefa: string;
  statusTarefa: StatusTarefa;
}

export interface Notificacao {
  id: number;
  nome: string;
  atividade: string;
  hora: string;
  dataNot: string;
  statusNot: string;
}

export interface Configuracao {
  id: number;
  nome: string;
  valor: string;
  qtdTarefa: number;
}

export interface Ministerio {
  id: number;
  nome: string;
  descricao: string;
}

export interface Evento {
  id: number;
  titulo: string;
  descricao: string;
  data: string;
  hora: string;
  local: string;
}

export interface Celula {
  id: number;
  nome: string;
  lider: string;
  endereco: string;
  diaSemana: string;
  horario: string;
  descricao: string;
}

export interface Profissional {
  id: number;
  nome: string;
  especialidade: string;
  telefone: string;
  email: string;
  foto: string;
  descricao: string;
}

export enum NivelAcesso {
  PASTOR_PRESIDENTE = 'PASTOR_PRESIDENTE',
  PASTOR_AUXILIAR = 'PASTOR_AUXILIAR',
  TESOUREIRO = 'TESOUREIRO',
  SECRETARIO = 'SECRETARIO'
}

export enum StatusMembro {
  ATIVO = 'ATIVO',
  INATIVO = 'INATIVO'
}

export enum StatusTarefa {
  PENDENTE = 'PENDENTE',
  CONCLUIDA = 'CONCLUIDA',
  CANCELADA = 'CANCELADA'
}

export enum StatusConta {
  PENDENTE = 'PENDENTE',
  PAGA = 'PAGA',
  CANCELADA = 'CANCELADA',
  VENCIDA = 'VENCIDA'
}

export interface AlterarSenhaRequest {
  senhaAtual: string;
  novaSenha: string;
}

export interface EsqueciSenhaRequest {
  email: string;
}

export interface RedefinirSenhaRequest {
  token: string;
  novaSenha: string;
}

export interface PerfilResponse {
  id: number;
  nome: string;
  email: string;
  documento: string;
  foto: string;
  nivel: string;
}

export interface AtualizarPerfilRequest {
  nome: string;
  email: string;
  foto: string;
}

export interface ApiMessageResponse {
  message: string;
}

export enum FrequenciaPagamento {
  UNICA = 'UNICA',
  MENSAL = 'MENSAL',
  TRIMESTRAL = 'TRIMESTRAL',
  SEMESTRAL = 'SEMESTRAL',
  ANUAL = 'ANUAL'
}
