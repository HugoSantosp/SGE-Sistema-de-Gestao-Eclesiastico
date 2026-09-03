import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../services/toast.service';
import { FilterConfig } from '../components/search-filter/search-filter.component';

@Component({
  selector: 'app-generic-list',
  templateUrl: './generic-list.component.html',
  styles: [':host { display: contents; }']
})
export class GenericListComponent implements OnInit {
  title = '';
  apiUrl = '';
  icon = 'bi-list';
  columns: string[] = [];
  extraActions: { label: string; icon: string; class: string; api: string; method: string; confirm: string }[] = [];
  itens: any[] = [];
  loading = true;
  searchTerm = '';
  
  // Paginação
  currentPage = 1;
  itemsPerPage = 10;
  totalItems = 0;
  
  // Filtros
  filters: FilterConfig[] = [];
  activeFilters: Record<string, any> = {};
  
  // Confirmação de exclusão
  showDeleteConfirm = false;
  itemToDelete: any = null;
  deleting = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    const data = this.route.snapshot.data;
    this.title = data['title'] || 'Listagem';
    this.apiUrl = data['api'] || '';
    this.icon = data['icon'] || 'bi-list';
    this.columns = data['columns'] || [];
    this.extraActions = data['extraActions'] || [];
    this.filters = data['filters'] || [];
    this.itemsPerPage = data['itemsPerPage'] || 10;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (d) => { 
        this.itens = d; 
        this.totalItems = d.length;
        this.loading = false; 
      },
      error: () => {
        this.loading = false;
        this.toastService.error('Erro ao carregar dados');
      }
    });
  }

  get filtered(): any[] {
    let result = [...this.itens];
    
    // Aplica busca
    if (this.searchTerm) {
      const s = this.searchTerm.toLowerCase();
      result = result.filter(i => 
        this.columns.some(c => String(i[c] || '').toLowerCase().includes(s))
      );
    }
    
    // Aplica filtros
    Object.entries(this.activeFilters).forEach(([key, value]) => {
      if (value !== '' && value !== null && value !== undefined && key !== 'search') {
        result = result.filter(i => {
          const itemValue = i[key];
          if (Array.isArray(value)) {
            return value.includes(itemValue);
          }
          return String(itemValue || '').toLowerCase().includes(String(value).toLowerCase());
        });
      }
    });
    
    return result;
  }

  get paginatedItems(): any[] {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filtered.slice(start, end);
  }

  get baseRoute(): string {
    const urlTree = this.router.parseUrl(this.router.url);
    const segments = urlTree.root.children['primary']?.segments.map(s => s.path) || [];
    const last = segments[segments.length - 1];
    if (last === 'novo' || last === 'editar') {
      return '/' + segments.slice(0, -1).join('/');
    }
    return '/' + segments.join('/');
  }

  editar(id: number): void {
    this.router.navigate([this.baseRoute, 'editar', id]);
  }

  novo(): void {
    this.router.navigate([this.baseRoute, 'novo']);
  }

  // Abre modal de confirmação antes de deletar
  confirmDelete(item: any): void {
    this.itemToDelete = item;
    this.showDeleteConfirm = true;
  }

  // Cancela exclusão
  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.itemToDelete = null;
  }

  // Executa exclusão após confirmação
  deletar(): void {
    if (!this.itemToDelete) return;
    
    this.deleting = true;
    this.http.delete(`${this.apiUrl}/${this.itemToDelete.id}`).subscribe({
      next: () => {
        this.toastService.success('Registro excluído com sucesso!');
        this.showDeleteConfirm = false;
        this.itemToDelete = null;
        this.deleting = false;
        this.load();
      },
      error: () => {
        this.toastService.error('Erro ao excluir registro');
        this.deleting = false;
      }
    });
  }

  executarAcaoExtra(acao: any, item: any): void {
    if (acao.confirm) {
      if (confirm(acao.confirm)) {
        this.executeAction(acao, item);
      }
    } else {
      this.executeAction(acao, item);
    }
  }

  private executeAction(acao: any, item: any): void {
    const url = acao.api.replace('{id}', item.id);
    const method = acao.method?.toLowerCase() || 'post';

    const request: any = method === 'delete'
      ? this.http.delete(url)
      : method === 'put'
        ? this.http.put(url, {})
        : this.http.post(url, {});

    request.subscribe({
      next: () => {
        this.toastService.success('Ação executada com sucesso!');
        this.load();
      },
      error: () => {
        this.toastService.error('Erro ao executar ação');
      }
    });
  }

  // Eventos de paginação
  onPageChange(page: number): void {
    this.currentPage = page;
  }

  // Eventos de busca/filtro
  onSearchChange(term: string): void {
    this.searchTerm = term;
    this.currentPage = 1; // Volta para primeira página
  }

  onFilterChange(filters: Record<string, any>): void {
    this.activeFilters = filters;
    this.currentPage = 1; // Volta para primeira página
  }

  onClearFilters(): void {
    this.searchTerm = '';
    this.activeFilters = {};
    this.currentPage = 1;
  }

  formatValue(obj: any, col: string): string {
    const val = obj[col];
    if (val === null || val === undefined) return '-';
    if (typeof val === 'boolean') return val ? 'Sim' : 'Não';
    if (col === 'senhaTemporaria') return val ? '🔴 Temporária' : '✅ Própria';
    return String(val);
  }
}
