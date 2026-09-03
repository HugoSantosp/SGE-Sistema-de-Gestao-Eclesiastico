import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EscalaService, EscalaResponse } from '../escala.service';
import { ministerioUrl } from '../../../core/config/app-config';

@Component({
  selector: 'app-escala-list',
  templateUrl: './escala-list.component.html',
  styles: [':host { display: contents; }']
})
export class EscalaListComponent implements OnInit {
  escalas: EscalaResponse[] = [];
  loading = true;
  searchTerm = '';

  constructor(
    private service: EscalaService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => { this.escalas = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  get filtered(): EscalaResponse[] {
    if (!this.searchTerm) return this.escalas;
    const s = this.searchTerm.toLowerCase();
    return this.escalas.filter(e =>
      e.titulo.toLowerCase().includes(s) ||
      e.publicToken?.toLowerCase().includes(s)
    );
  }

  novo(): void {
    this.router.navigate(['/escalas/novo']);
  }

  editar(id: number): void {
    this.router.navigate(['/escalas', id]);
  }

  montar(id: number): void {
    this.router.navigate(['/escalas', id, 'montar']);
  }

  toggle(id: number): void {
    this.service.toggleEscala(id).subscribe(() => this.load());
  }

  deletar(id: number): void {
    if (confirm('Tem certeza que deseja excluir esta escala?')) {
      this.service.deletar(id).subscribe(() => this.load());
    }
  }

  gerarLink(id: number): void {
    this.service.gerarLinkResultado(id).subscribe({
      next: (res) => {
        const link = ministerioUrl(`/escala/resultado/${res.resultadoToken}`);
        navigator.clipboard.writeText(link).then(() => {
          alert('Link copiado para a área de transferência!');
        }).catch(() => {
          prompt('Link público do resultado:', link);
        });
      },
      error: () => alert('Erro ao gerar link')
    });
  }

  copiarLinkPublico(token: string): void {
    if (!token) return;
    const link = ministerioUrl(`/escala/${token}`);
    navigator.clipboard.writeText(link).then(() => {
      alert('Link público copiado!');
    }).catch(() => {
      prompt('Link público da escala:', link);
    });
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString('pt-BR');
  }
}
