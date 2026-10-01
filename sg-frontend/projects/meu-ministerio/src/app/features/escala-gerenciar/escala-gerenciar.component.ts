import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { mmSelfUrl } from '../../core/config/mm-config';
import {
  EscalaDetalhada, DataDetalhada, ConfirmacaoResponse,
  EscalaDataDTO, DesignacaoRequest, MusicaSimple
} from '../../core/models/mm.models';

@Component({
  selector: 'app-escala-gerenciar',
  templateUrl: './escala-gerenciar.component.html',
  styleUrls: ['./escala-gerenciar.component.scss']
})
export class EscalaGerenciarComponent implements OnInit {
  escala: EscalaDetalhada | null = null;
  escalaId: number | null = null;
  loading = true;
  error = '';
  salvando = false;
  aviso = '';

  /** Rótulo da opção que revela o campo de texto livre */
  readonly OUTRO = 'Outro…';

  instrumentos: string[] = [];

  // Designações em edição por data
  designacoes: { [dataId: number]: DesignacaoRequest[] } = {};
  // Músicas em edição por data
  musicas: { [dataId: number]: MusicaSimple[] } = {};
  /** Funções "Outro…" com texto livre, por data: índice da designação → texto digitado */
  outroTexto: { [dataId: number]: { [index: number]: string } } = {};

  // Form nova data
  novaData: EscalaDataDTO = { nomeEvento: '', data: '', horario: '', local: '' };
  adicionandoData = false;

  // Link do resultado
  resultadoLink = '';
  gerandoLink = false;

  constructor(
    private api: MmApiService,
    private authService: MmAuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.escalaId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.escalaId) {
      this.router.navigate(['/escalas']);
      return;
    }
    this.api.listarInstrumentos().subscribe({
      next: (data) => { this.instrumentos = [...data, this.OUTRO]; },
      error: () => {
        this.instrumentos = ['Ministro', 'Vocalista', 'Backing Vocal', 'Guitarra', 'Violão', 'Baixo', 'Bateria', 'Teclado', 'Sax', 'Técnico de Som', 'Projeção', 'Câmera/Live', 'Iluminação', 'Recepção', 'Intercessão', 'Diaconato', 'Crianças', this.OUTRO];
      }
    });
    this.carregar();
  }

  carregar(): void {
    if (!this.escalaId) return;
    this.loading = true;
    this.error = '';
    this.api.buscarEscala(this.escalaId).subscribe({
      next: (escala) => {
        this.escala = escala;
        escala.datas.forEach(d => {
          this.designacoes[d.id] = d.designacoes.map(des => ({
            confirmacaoId: des.confirmacaoId,
            instrumento: des.instrumento,
            ordem: des.ordem
          }));
          this.musicas[d.id] = d.musicas.map(m => ({ ...m }));
          this.outroTexto[d.id] = this.outroTexto[d.id] || {};
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = err.status === 422 || err.status === 400
          ? 'Acesso negado — apenas o líder deste ministério pode montar a escala.'
          : 'Erro ao carregar a escala.';
        this.loading = false;
      }
    });
  }

  /**
   * Opções do select de função para uma designação:
   * lista curada + "Outro…"; se a função salva é customizada (texto livre),
   * ela aparece como opção selecionável diretamente no select.
   */
  opcoesFuncao(des: DesignacaoRequest): string[] {
    if (des.instrumento && des.instrumento !== this.OUTRO && !this.instrumentos.includes(des.instrumento)) {
      const semOutro = this.instrumentos.filter(i => i !== this.OUTRO);
      return [...semOutro, des.instrumento, this.OUTRO];
    }
    return this.instrumentos;
  }

  confirmadosDaData(data: DataDetalhada): ConfirmacaoResponse[] {
    if (!this.escala) return [];
    return this.escala.confirmacoes.filter(c => c.dataIds.includes(data.id));
  }

  // ===== Datas =====

  adicionarData(): void {
    if (!this.escalaId) return;
    if (!this.novaData.nomeEvento || !this.novaData.data || !this.novaData.horario) {
      this.error = 'Preencha nome, data e horário';
      return;
    }
    this.adicionandoData = true;
    this.error = '';
    this.api.adicionarData(this.escalaId, this.novaData).subscribe({
      next: () => {
        this.adicionandoData = false;
        this.novaData = { nomeEvento: '', data: '', horario: '', local: '' };
        this.carregar();
      },
      error: () => {
        this.adicionandoData = false;
        this.error = 'Erro ao adicionar a data.';
      }
    });
  }

  removerData(dataId: number): void {
    if (!this.escalaId) return;
    if (!confirm('Remover esta data da escala?')) return;
    this.api.removerData(this.escalaId, dataId).subscribe({
      next: () => this.carregar(),
      error: () => this.error = 'Erro ao remover a data.'
    });
  }

  // ===== Designações =====

  adicionarDesignacao(dataId: number): void {
    const data = this.escala?.datas.find(d => d.id === dataId);
    if (!data) return;
    const confirmados = this.confirmadosDaData(data);
    if (confirmados.length === 0) {
      this.aviso = 'Não há confirmações para esta data. Compartilhe o link público primeiro.';
      return;
    }
    if (!this.designacoes[dataId]) this.designacoes[dataId] = [];
    this.outroTexto[dataId] = this.outroTexto[dataId] || {};
    this.designacoes[dataId].push({
      confirmacaoId: confirmados[0].id,
      instrumento: this.instrumentos[0] || 'Ministro',
      ordem: this.designacoes[dataId].length
    });
  }

  removerDesignacao(dataId: number, index: number): void {
    this.designacoes[dataId].splice(index, 1);
    // Reindexa os textos "Outro…" para acompanhar os novos índices
    const mapa = this.outroTexto[dataId] || {};
    delete mapa[index];
    const novo: { [i: number]: string } = {};
    Object.keys(mapa).forEach(k => {
      const i = Number(k);
      novo[i > index ? i - 1 : i] = mapa[i];
    });
    this.outroTexto[dataId] = novo;
  }

  salvarDesignacoes(dataId: number): void {
    if (!this.escalaId) return;
    // "Outro…" vira o texto livre digitado
    const lista = (this.designacoes[dataId] || []).map((des, i) => ({
      ...des,
      instrumento: des.instrumento === this.OUTRO
        ? (this.outroTexto[dataId]?.[i] || '').trim()
        : des.instrumento
    }));
    if (lista.some(d => !d.instrumento)) {
      this.error = 'Preencha a função da designação marcada como "Outro…".';
      return;
    }
    this.salvando = true;
    this.error = '';
    this.api.salvarDesignacoes(this.escalaId, dataId, lista).subscribe({
      next: () => {
        this.salvando = false;
        this.aviso = 'Designações salvas com sucesso!';
        this.carregar();
      },
      error: () => {
        this.salvando = false;
        this.error = 'Erro ao salvar designações.';
      }
    });
  }

  setOutroTexto(dataId: number, index: number, valor: string): void {
    this.outroTexto[dataId] = this.outroTexto[dataId] || {};
    this.outroTexto[dataId][index] = valor;
  }

  /** Ao escolher "Outro…", pré-preenche o campo com a função customizada anterior, se houver. */
  onFuncaoChange(dataId: number, index: number, des: DesignacaoRequest, novo: string): void {
    if (novo === this.OUTRO) {
      const anterior = des.instrumento;
      this.outroTexto[dataId] = this.outroTexto[dataId] || {};
      if (anterior && anterior !== this.OUTRO && !this.instrumentos.includes(anterior)
          && !this.outroTexto[dataId][index]) {
        this.outroTexto[dataId][index] = anterior;
      }
    }
    des.instrumento = novo;
  }

  // ===== Músicas =====

  adicionarMusica(dataId: number): void {
    if (!this.musicas[dataId]) this.musicas[dataId] = [];
    this.musicas[dataId].push({ nome: '', artista: '', link: '', ordem: this.musicas[dataId].length });
  }

  removerMusica(dataId: number, index: number): void {
    this.musicas[dataId].splice(index, 1);
  }

  salvarMusicas(dataId: number): void {
    if (!this.escalaId) return;
    this.salvando = true;
    this.error = '';
    this.api.salvarMusicas(this.escalaId, dataId, (this.musicas[dataId] || []).filter(m => m.nome.trim())).subscribe({
      next: () => {
        this.salvando = false;
        this.aviso = 'Repertório salvo!';
        this.carregar();
      },
      error: () => {
        this.salvando = false;
        this.error = 'Erro ao salvar o repertório.';
      }
    });
  }

  // ===== Escala =====

  toggle(): void {
    if (!this.escalaId) return;
    this.api.toggleEscala(this.escalaId).subscribe({
      next: () => this.carregar(),
      error: () => this.error = 'Erro ao alterar a escala.'
    });
  }

  gerarLink(): void {
    if (!this.escalaId) return;
    this.gerandoLink = true;
    this.error = '';
    this.api.gerarLinkResultado(this.escalaId).subscribe({
      next: (res) => {
        this.gerandoLink = false;
        // O resultado público fica neste próprio app (rota /escala/resultado/:token)
        this.resultadoLink = mmSelfUrl(`/escala/resultado/${res.resultadoToken}`);
      },
      error: () => {
        this.gerandoLink = false;
        this.error = 'Erro ao gerar o link.';
      }
    });
  }

  copiarLink(): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(this.resultadoLink);
      this.aviso = 'Link copiado!';
    }
  }

  voltar(): void {
    if (this.escalaId) {
      this.router.navigate(['/escalas', this.escalaId]);
    } else {
      this.router.navigate(['/escalas']);
    }
  }

  sair(): void {
    this.authService.logout();
  }

  formatData(dataISO: string): string {
    const [y, m, d] = dataISO.split('-');
    return `${d}/${m}/${y}`;
  }
}
