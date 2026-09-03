import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MembrosService } from '../membros.service';
import { Membro, Ministerio } from '../../../core/models/sge.models';

@Component({
  selector: 'app-membros-list',
  templateUrl: './membros-list.component.html',
  styles: [':host { display: contents; }']
})
export class MembrosListComponent implements OnInit {
  membros: Membro[] = [];
  ministerios: Ministerio[] = [];
  loading = true;
  searchTerm = '';

  constructor(
    private service: MembrosService,
    private http: HttpClient,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.http.get<Ministerio[]>('/api/ministerios').subscribe({
      next: (data) => { this.ministerios = data; }
    });
    this.load();
  }

  getMinisterioNome(ministerioId: number | null | undefined): string {
    if (!ministerioId) return '-';
    const m = this.ministerios.find(m => m.id === ministerioId);
    return m ? m.nome : '-';
  }

  load(): void {
    this.loading = true;
    this.service.listar().subscribe({
      next: (data) => { this.membros = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  get filtered(): Membro[] {
    if (!this.searchTerm) return this.membros;
    const s = this.searchTerm.toLowerCase();
    return this.membros.filter(m =>
      m.nome.toLowerCase().includes(s) ||
      m.documento?.toLowerCase().includes(s) ||
      m.telefone?.toLowerCase().includes(s)
    );
  }

  editar(id: number): void { this.router.navigate(['/membros/editar', id]); }

  novo(): void { this.router.navigate(['/membros/novo']); }

  deletar(id: number): void {
    if (confirm('Tem certeza que deseja excluir este membro?')) {
      this.service.deletar(id).subscribe(() => this.load());
    }
  }

  statusClass(situacao: string): string {
    return situacao === 'ATIVO' ? 'badge-success' : 'badge-warning';
  }
}
