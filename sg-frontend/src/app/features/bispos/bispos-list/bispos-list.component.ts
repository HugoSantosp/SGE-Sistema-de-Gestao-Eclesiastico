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

  editar(id: number): void { this.router.navigate(['/bispos/editar', id]); }
  novo(): void { this.router.navigate(['/bispos/novo']); }

  deletar(id: number): void {
    if (confirm('Tem certeza?')) this.service.deletar(id).subscribe(() => this.load());
  }
}
