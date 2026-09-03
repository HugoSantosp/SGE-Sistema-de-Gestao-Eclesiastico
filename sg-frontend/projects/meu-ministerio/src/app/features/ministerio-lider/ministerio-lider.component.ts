import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import {
  MembroMinisterio, MembroSimple, EscalaResponse,
  PapelMinisterio, PAPEL_MINISTERIO_LIST, PAPEL_MINISTERIO_LABELS, papelMinisterioLabel
} from '../../core/models/mm.models';

@Component({
  selector: 'app-ministerio-lider',
  templateUrl: './ministerio-lider.component.html',
  styleUrls: ['./ministerio-lider.component.scss']
})
export class MinisterioLiderComponent implements OnInit {
  ministerioId: number | null = null;
  ministerioNome = 'Ministério';
  loading = true;
  error = '';

  // Membros
  membros: MembroMinisterio[] = [];
  membrosDisponiveis: MembroSimple[] = [];
  membroSelecionado: number | null = null;
  papelNovo: PapelMinisterio = 'INTEGRANTE';
  carregandoMembros = false;
  salvandoMembro = false;

  // Papéis disponíveis para selects
  papeis: PapelMinisterio[] = PAPEL_MINISTERIO_LIST;
  papelLabels = PAPEL_MINISTERIO_LABELS;

  // Papel do usuário logado neste ministério (null = pastor sem vínculo)
  meuPapel: PapelMinisterio | null = null;
  isPastor = false;

  /** Helper de template para rótulos de papel */
  papelLabel = papelMinisterioLabel;

  // Escalas
  escalas: EscalaResponse[] = [];
  carregandoEscalas = false;
  novoTitulo = '';
  criandoEscala = false;

  aba = 'membros'; // 'membros' | 'escalas'

  constructor(
    private api: MmApiService,
    private authService: MmAuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.ministerioId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.ministerioId) {
      this.router.navigate(['/ministerios']);
      return;
    }
    // Busca o nome do ministério e o papel do usuário logado nele
    const nivel = this.authService.getCurrentUser()?.nivel || '';
    this.isPastor = nivel === 'PASTOR_PRESIDENTE' || nivel === 'PASTOR_AUXILIAR';
    this.api.meusMinisterios().subscribe({
      next: (lista) => {
        const m = lista.find(x => x.id === this.ministerioId);
        if (m) {
          this.ministerioNome = m.nome;
          this.meuPapel = m.papel;
        }
      },
      error: () => {}
    });
    this.carregarMembros();
    this.carregarEscalas();
  }

  // ===== Membros =====

  carregarMembros(): void {
    if (!this.ministerioId) return;
    this.loading = true;
    this.error = '';
    this.api.membrosDoMinisterio(this.ministerioId).subscribe({
      next: (lista) => {
        this.membros = lista;
        this.loading = false;
        // Carrega a lista de disponíveis após ter os atuais (para filtrar corretamente)
        this.carregarDisponiveis();
      },
      error: (err) => {
        this.error = err.status === 422 || err.status === 400
          ? 'Acesso negado — apenas o líder pode gerenciar este ministério.'
          : 'Erro ao carregar membros.';
        this.loading = false;
      }
    });
  }

  /** Carrega os membros da igreja que ainda NÃO estão neste ministério (para o select de adicionar). */
  carregarDisponiveis(): void {
    if (!this.ministerioId) return;
    this.carregandoMembros = true;
    this.api.listarMembros().subscribe({
      next: (todos) => {
        // Filtra os que já estão no ministério
        const jaNoMinisterio = new Set(this.membros.map(m => m.membroId));
        this.membrosDisponiveis = todos.filter(m => !jaNoMinisterio.has(m.id));
        this.carregandoMembros = false;
      },
      error: () => {
        this.carregandoMembros = false;
        this.error = 'Erro ao carregar membros disponíveis.';
      }
    });
  }

  adicionarMembro(): void {
    if (!this.ministerioId || !this.membroSelecionado) {
      this.error = 'Selecione um membro';
      return;
    }
    this.salvandoMembro = true;
    this.error = '';
    this.api.adicionarMembro(this.ministerioId, {
      membroId: this.membroSelecionado,
      papel: this.papelNovo
    }).subscribe({
      next: () => {
        this.salvandoMembro = false;
        this.membroSelecionado = null;
        this.carregarMembros();
      },
      error: () => {
        this.salvandoMembro = false;
        this.error = 'Erro ao adicionar membro.';
      }
    });
  }

  mudarPapel(membro: MembroMinisterio, papel: string): void {
    if (!this.ministerioId) return;
    const novoPapel = papel as PapelMinisterio;
    this.api.alterarPapel(this.ministerioId, membro.membroId, novoPapel).subscribe({
      next: () => this.carregarMembros(),
      error: () => this.error = 'Erro ao alterar o papel.'
    });
  }

  removerMembro(membro: MembroMinisterio): void {
    if (!this.ministerioId) return;
    if (!confirm(`Remover ${membro.nome} deste ministério?`)) return;
    this.api.removerMembro(this.ministerioId, membro.membroId).subscribe({
      next: () => this.carregarMembros(),
      error: () => this.error = 'Erro ao remover membro.'
    });
  }

  // ===== Escalas =====

  carregarEscalas(): void {
    if (!this.ministerioId) return;
    this.carregandoEscalas = true;
    this.api.escalasDoMinisterio(this.ministerioId).subscribe({
      next: (lista) => {
        this.escalas = lista;
        this.carregandoEscalas = false;
      },
      error: () => {
        this.carregandoEscalas = false;
      }
    });
  }

  criarEscala(): void {
    if (!this.ministerioId || !this.novoTitulo.trim()) {
      this.error = 'Digite um título para a escala';
      return;
    }
    this.criandoEscala = true;
    this.error = '';
    this.api.criarEscala(this.ministerioId, this.novoTitulo.trim()).subscribe({
      next: (escala) => {
        this.criandoEscala = false;
        this.novoTitulo = '';
        this.router.navigate(['/escalas', escala.id, 'gerenciar']);
      },
      error: () => {
        this.criandoEscala = false;
        this.error = 'Erro ao criar escala.';
      }
    });
  }

  abrirMontagem(escalaId: number): void {
    this.router.navigate(['/escalas', escalaId, 'gerenciar']);
  }

  voltar(): void {
    this.router.navigate(['/ministerios']);
  }

  sair(): void {
    this.authService.logout();
  }
}
