import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { EscalaResponse } from '../../core/models/mm.models';

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

  abrirEscala(id: number): void {
    this.router.navigate(['/escalas', id]);
  }

  sair(): void {
    this.authService.logout();
  }

  formatarData(dataISO: string): string {
    if (!dataISO) return '';
    const d = new Date(dataISO);
    return d.toLocaleDateString('pt-BR');
  }
}
