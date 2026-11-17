import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BisposService } from '../bispos.service';
import { Bispo } from '../../../core/models/sge.models';

@Component({
  selector: 'app-bispos-list',
  templateUrl: './bispos-list.component.html',
  styles: [':host { display: contents; }']
})
export class BisposListComponent implements OnInit {
  itens: Bispo[] = [];
  loading = true;
  searchTerm = '';

  // Paginação: 10 registros por página
  currentPage = 1;
  itemsPerPage = 10;

  constructor(private service: BisposService, private router: Router) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.service.listar().subscribe({ next: (d) => { this.itens = d; this.loading = false; }, error: () => this.loading = false });
  }

  get filtered(): Bispo[] {
    if (!this.searchTerm) return this.itens;
    const s = this.searchTerm.toLowerCase();
    return this.itens.filter(i => i.nome.toLowerCase().includes(s));
  }

  /** Volta para a primeira página ao digitar na busca. */
  onBuscaChange(): void {
    this.currentPage = 1;
  }

  onPageChange(page: number): void {
    this.currentPage = page;
  }

  get paginatedItems(): Bispo[] {
    const inicio = (this.currentPage - 1) * this.itemsPerPage;
    return this.filtered.slice(inicio, inicio + this.itemsPerPage);
  }

  editar(id: number): void { this.router.navigate(['/bispos/editar', id]); }
  novo(): void { this.router.navigate(['/bispos/novo']); }

  deletar(id: number): void {
    if (confirm('Tem certeza?')) this.service.deletar(id).subscribe(() => this.load());
  }
}
