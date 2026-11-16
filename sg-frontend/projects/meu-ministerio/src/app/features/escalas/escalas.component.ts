import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { EscalaResponse, MinisterioDoUsuario } from '../../core/models/mm.models';

@Component({
  selector: 'app-escalas',
  templateUrl: './escalas.component.html',
  styleUrls: ['./escalas.component.scss']
})
export class EscalasComponent implements OnInit {
  escalas: EscalaResponse[] = [];
  loading = true;
  error = '';
  ministerioFiltro: number | null = null;

  /** id do ministério → foto (null quando não cadastrada). */
  private fotoPorMinisterio = new Map<number, string | null>();
  /** id do ministério → nome, para o fallback com a inicial. */
  private nomePorMinisterio = new Map<number, string>();

  constructor(
    private api: MmApiService,
    private authService: MmAuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.ministerioFiltro = params['ministerio'] ? Number(params['ministerio']) : null;
      this.carregar();
    });
  }

  carregar(): void {
    this.loading = true;
    this.error = '';
    this.carregarMinisterios();
    this.api.minhasEscalas().subscribe({
      next: (lista) => {
        this.escalas = this.ministerioFiltro
          ? lista.filter(e => e.ministerioId === this.ministerioFiltro)
          : lista;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erro ao carregar as escalas';
        this.loading = false;
      }
    });
  }

  /**
   * Carrega foto e nome dos ministérios do usuário para exibir no card.
   * Falha silenciosa: sem isso o card cai no fallback com a inicial.
   */
  private carregarMinisterios(): void {
    this.api.meusMinisterios().subscribe({
      next: (lista: MinisterioDoUsuario[]) => {
        this.fotoPorMinisterio.clear();
        this.nomePorMinisterio.clear();
        lista.forEach(m => {
          this.fotoPorMinisterio.set(m.id, m.foto);
          this.nomePorMinisterio.set(m.id, m.nome);
        });
      },
      error: () => {}
    });
  }

  /** Foto do ministério da escala, quando houver. */
  fotoDe(e: EscalaResponse): string | null {
    if (e.ministerioId == null) return null;
    return this.fotoPorMinisterio.get(e.ministerioId) ?? null;
  }

  /** Inicial usada quando o ministério não tem foto. */
  inicialDe(e: EscalaResponse): string {
    const nome = (e.ministerioId != null ? this.nomePorMinisterio.get(e.ministerioId) : null)
      || e.nomeMinisterio
      || e.titulo
      || '?';
    return nome.trim().charAt(0).toUpperCase();
  }

  abrirEscala(id: number): void {
    this.router.navigate(['/escalas', id]);
  }

  sair(): void {
    this.authService.logout();
  }

  formatarData(dataISO: string): string {
    if (!dataISO) return '';
    const [y, m, d] = dataISO.split('-');
    return `${d}/${m}/${y}`;
  }
}
