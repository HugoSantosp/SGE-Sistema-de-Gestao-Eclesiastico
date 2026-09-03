import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { EscalaDetalhada } from '../../core/models/mm.models';

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
      },
      error: () => {
        this.error = 'Não foi possível carregar esta escala.';
        this.loading = false;
      }
    });
  }

  toggleData(dataId: number): void {
    if (!this.escala?.aberta) return;
    if (this.selecionadas.has(dataId)) {
      this.selecionadas.delete(dataId);
    } else {
      this.selecionadas.add(dataId);
    }
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
        this.error = err.status === 403
          ? 'Acesso negado. Você não participa deste ministério.'
          : 'Erro ao confirmar. Tente novamente.';
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

  diaNum(dataISO: string): string {
    return new Date(dataISO).getDate().toString();
  }

  diaMes(dataISO: string): string {
    const meses = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ'];
    return meses[new Date(dataISO).getMonth()];
  }

  diaSemana(dataISO: string): string {
    const dias = ['domingo', 'segunda', 'terça', 'quarta', 'quinta', 'sexta', 'sábado'];
    return dias[new Date(dataISO).getDay()];
  }

  jaConfirmado(dataId: number): boolean {
    return this.minhasDataIds.includes(dataId);
  }
}
