import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MmApiService } from '../../core/services/mm-api.service';
import { MmAuthService } from '../../core/auth/mm-auth.service';
import { MinisterioDoUsuario, papelMinisterioLabel } from '../../core/models/mm.models';

@Component({
  selector: 'app-ministerios',
  templateUrl: './ministerios.component.html',
  styleUrls: ['./ministerios.component.scss']
})
export class MinisteriosComponent implements OnInit {
  ministerios: MinisterioDoUsuario[] = [];
  liderados: MinisterioDoUsuario[] = [];
  loading = true;
  error = '';
  userName = '';
  isPastor = false;

  /** Helper de template para rótulos de papel */
  papelLabel = papelMinisterioLabel;

  constructor(
    private api: MmApiService,
    private authService: MmAuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userName = this.authService.getCurrentUser()?.nome?.split(' ')[0] || '';
    const nivel = this.authService.getCurrentUser()?.nivel || '';
    this.isPastor = nivel === 'PASTOR_PRESIDENTE' || nivel === 'PASTOR_AUXILIAR';
    this.carregar();
  }

  carregar(): void {
    this.loading = true;
    this.error = '';
    this.api.meusMinisterios().subscribe({
      next: (lista) => {
        this.ministerios = lista;
        // Pastores têm acesso ampliado: gerenciam qualquer ministério.
        // Membros só gerenciam onde possuem papel LIDER.
        this.liderados = this.isPastor ? lista : lista.filter(m => m.papel === 'LIDER');
        this.loading = false;
      },
      error: () => {
        this.error = 'Erro ao carregar seus ministérios';
        this.loading = false;
      }
    });
  }

  abrirMinisterio(id: number): void {
    this.router.navigate(['/escalas'], { queryParams: { ministerio: id } });
  }

  gerenciarMinisterio(id: number): void {
    this.router.navigate(['/ministerios', id, 'gerenciar']);
  }

  sair(): void {
    this.authService.logout();
  }
}
