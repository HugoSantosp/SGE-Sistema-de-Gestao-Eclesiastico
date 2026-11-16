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
  situacaoFiltro: '' | 'ATIVO' | 'INATIVO' = '';

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
    this.service.listar(this.situacaoFiltro || undefined).subscribe({
      next: (data) => { this.membros = data; this.loading = false; },
      error: () => { this.membros = []; this.loading = false; }
    });
  }

  /** Recarrega a lista no servidor ao trocar o filtro de situação. */
  filtrarPorSituacao(situacao: '' | 'ATIVO' | 'INATIVO'): void {
    this.situacaoFiltro = situacao;
    this.load();
  }

  /**
   * Normaliza texto para busca: remove acentos, ignora maiúsculas/minúsculas
   * e espaços nas pontas. Sem isso, "jose" não encontrava "José".
   */
  private normalizar(valor: string | null | undefined): string {
    if (!valor) return '';
    return valor
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }

  get filtered(): Membro[] {
    const termo = this.normalizar(this.searchTerm);
    if (!termo) return this.membros;

    // Busca também por documento sem máscara (ex.: "123.456" acha "12345678900").
    const digitos = termo.replace(/\D/g, '');

    return this.membros.filter(m => {
      const nome = this.normalizar(m.nome);
      const documento = this.normalizar(m.documento);
      const telefone = this.normalizar(m.telefone);

      if (nome.includes(termo) || documento.includes(termo) || telefone.includes(termo)) {
        return true;
      }
      return digitos.length > 0 && documento.replace(/\D/g, '').includes(digitos);
    });
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
