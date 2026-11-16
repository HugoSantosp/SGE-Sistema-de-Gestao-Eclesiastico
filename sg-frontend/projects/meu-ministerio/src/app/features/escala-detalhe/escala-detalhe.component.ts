import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { EscalaConflito, EscalaDetalhada } from '../../core/models/mm.models';

@Component({
  selector: 'app-escala-detalhe',
  templateUrl: './escala-detalhe.component.html',
  styleUrls: ['./escala-detalhe.component.scss']
})
export class EscalaDetalheComponent implements OnInit {
  escala: EscalaDetalhada | null = null;
  loading = true;
  error = '';
  salvando = false;
  sucesso = '';

  // Datas selecionadas para confirmar (por id)
  selecionadas = new Set<number>();

  // Confirmação atual do usuário (dataIds já confirmados)
  minhasDataIds: number[] = [];

  // Datas desta escala já confirmadas em outro ministério (mesmo dia e horário)
  conflitos: EscalaConflito[] = [];
  datasBloqueadas = new Set<number>();
  conflitoPorData = new Map<number, EscalaConflito>();

  // idPessoa (Membro.id) do usuário logado — obtido via /auth/me
  meuMembroId: number | null = null;

  constructor(
    private api: MmApiService,
    private authService: MmAuthService,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.router.navigate(['/escalas']);
      return;
    }
    // Busca o idPessoa (Membro.id) do usuário para identificar suas confirmações
    this.http.get<any>('/auth/me').subscribe({
      next: (perfil) => {
        this.meuMembroId = perfil?.idPessoa ?? null;
        this.carregar(id);
      },
      error: () => this.carregar(id)
    });
  }

  carregar(id: number): void {
    this.loading = true;
    this.error = '';
    this.api.buscarEscala(id).subscribe({
      next: (escala) => {
        this.escala = escala;
        // Pré-marca as datas já confirmadas pelo usuário (via membroId real)
        if (this.meuMembroId !== null) {
          const minha = escala.confirmacoes.find(c => c.membroId === this.meuMembroId);
          if (minha) {
            this.minhasDataIds = minha.dataIds;
            minha.dataIds.forEach(d => this.selecionadas.add(d));
          }
        }
        this.loading = false;
        this.carregarConflitos(id);
      },
      error: () => {
        this.error = 'Não foi possível carregar esta escala.';
        this.loading = false;
      }
    });
  }

  /**
   * Carrega as datas que o usuário já confirmou em outro ministério no mesmo dia e
   * horário e as marca como bloqueadas, para ele não descobrir o choque só ao salvar.
   * Falha em silêncio: o servidor continua bloqueando na confirmação.
   */
  carregarConflitos(id: number): void {
    this.api.buscarConflitos(id).subscribe({
      next: (lista) => {
        this.conflitos = lista;
        this.datasBloqueadas.clear();
        this.conflitoPorData.clear();

        const porChave = new Map<string, EscalaConflito>();
        lista.forEach(c => porChave.set(this.chave(c.data, c.horario), c));

        (this.escala?.datas ?? []).forEach(d => {
          const conflito = porChave.get(this.chave(d.data, d.horario));
          if (conflito) {
            this.datasBloqueadas.add(d.id);
            this.conflitoPorData.set(d.id, conflito);
            this.selecionadas.delete(d.id);
          }
        });
      },
      error: () => {
        this.conflitos = [];
        this.datasBloqueadas.clear();
        this.conflitoPorData.clear();
      }
    });
  }

  toggleData(dataId: number): void {
    if (!this.escala?.aberta) return;
    if (this.datasBloqueadas.has(dataId)) {
      this.error = this.motivo(dataId);
      return;
    }
    if (this.selecionadas.has(dataId)) {
      this.selecionadas.delete(dataId);
    } else {
      this.selecionadas.add(dataId);
    }
  }

  /** Texto do bloqueio de uma data, usado no title e no aviso ao clicar. */
  motivo(dataId: number): string {
    const conflito = this.conflitoPorData.get(dataId);
    if (!conflito) {
      return 'Data indisponível em outro ministério.';
    }
    return `Indisponível — já confirmado em ${conflito.ministerioNome} (${conflito.escalaTitulo}).`;
  }

  /** Nome do ministério que bloqueia a data (vazio quando não há conflito). */
  ministerioDoBloqueio(dataId: number): string {
    return this.conflitoPorData.get(dataId)?.ministerioNome ?? '';
  }

  confirmar(): void {
    if (!this.escala) return;
    if (this.selecionadas.size === 0) {
      this.error = 'Selecione pelo menos uma data';
      return;
    }

    this.salvando = true;
    this.error = '';
    this.sucesso = '';

    const dataIds = Array.from(this.selecionadas);
    this.api.confirmarDisponibilidade(this.escala.id, dataIds).subscribe({
      next: () => {
        this.salvando = false;
        this.sucesso = 'Disponibilidade confirmada! 🎉';
        setTimeout(() => this.carregar(this.escala!.id), 800);
      },
      error: (err) => {
        this.salvando = false;
        if (err.status === 403) {
          this.error = 'Acesso negado. Você não participa deste ministério.';
        } else if (err.status === 422 && err.error?.message) {
          // Conflito de data + horário com outro ministério
          this.error = err.error.message;
        } else {
          this.error = 'Erro ao confirmar. Tente novamente.';
        }
      }
    });
  }

  cancelar(): void {
    if (!this.escala) return;
    this.salvando = true;
    this.error = '';
    this.sucesso = '';
    this.api.cancelarConfirmacao(this.escala.id).subscribe({
      next: () => {
        this.salvando = false;
        this.sucesso = 'Confirmação cancelada.';
        this.selecionadas.clear();
        this.minhasDataIds = [];
        setTimeout(() => this.carregar(this.escala!.id), 800);
      },
      error: () => {
        this.salvando = false;
        this.error = 'Erro ao cancelar. Tente novamente.';
      }
    });
  }

  sair(): void {
    this.authService.logout();
  }

  // ===== Helpers de formatação =====

  /**
   * Chave de comparação data + horário. O backend envia LocalTime como "HH:mm:ss";
   * comparamos só "HH:mm" para não depender do formato exato.
   */
  private chave(data: string, horario: string): string {
    return `${(data || '').slice(0, 10)}T${(horario || '').slice(0, 5)}`;
  }

  /**
   * Parseia uma data no formato yyyy-MM-dd manualmente (sem passar por
   * new Date(string), que interpreta como UTC e pode "voltar" 1 dia
   * em fusos negativos como o do Brasil).
   */
  private parseDataLocal(dataISO: string): Date {
    const [y, m, d] = dataISO.split('-').map(Number);
    return new Date(y, m - 1, d);
  }

  diaNum(dataISO: string): string {
    return this.parseDataLocal(dataISO).getDate().toString();
  }

  diaMes(dataISO: string): string {
    const meses = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ'];
    return meses[this.parseDataLocal(dataISO).getMonth()];
  }

  diaSemana(dataISO: string): string {
    const dias = ['domingo', 'segunda', 'terça', 'quarta', 'quinta', 'sexta', 'sábado'];
    return dias[this.parseDataLocal(dataISO).getDay()];
  }

  jaConfirmado(dataId: number): boolean {
    return this.minhasDataIds.includes(dataId);
  }
}
